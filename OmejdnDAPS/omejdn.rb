# frozen_string_literal: true

require 'rubygems'
require 'bundler/setup'

require_relative './lib/client'
require_relative './lib/config'
require_relative './lib/user'
require_relative './lib/token_helper'
require_relative './lib/oauth_helper'
require_relative './lib/user_db'
require 'sinatra'
require 'sinatra/cookies'
require 'sinatra/cors'
require 'sinatra/activerecord'
require 'securerandom'
require 'json/jwt'
require 'webrick'
require 'webrick/https'
require 'net/http'
require 'bcrypt'

OMEJDN_VERSION = '1.0.0'
OMEJDN_LICENSE = 'Apache2.0'

def debug
  ENV['APP_ENV'] != 'production'
end

def host
  ENV['HOST'] || Config.base_config['host']
end

def my_prefix
  ENV['OMEJDN_PATH_PREFIX'] || ''
end

def my_path
  host + my_prefix
end

configure do
  # Easier debugging for local tests
  set :raise_errors, debug && !ENV['HOST']
  set :show_exceptions, debug && ENV['HOST']
end

set :bind, ENV['BIND_TO'] || '0.0.0.0'
enable :sessions
set :sessions, secure: (host.start_with? 'https://')
set :session_store, Rack::Session::Pool

set :allow_origin, ENV['ALLOW_ORIGIN'] || 'http://localhost:4200'
set :allow_methods, 'GET,HEAD,POST,PUT,DELETE'
set :allow_headers, 'content-type,if-modified-since, authorization'
set :expose_headers, 'location,link'
set :allow_credentials, true

# A User session dummy class. We probably want to use
# a KV-store at some point
class UserSession
  @user_session = {}
  def self.get
    @user_session
  end
end

# The format of this cache data structure is:
#
# {
#   <authorization code> => {
#                             :user => User,
#                             :nonce => <oauth nonce> (optional)
#                             :scopes => Requested scopes
#                             :claims => claim parameter
#                             :pkce => Code challenge
#                             :pkce_method => Code challenge method
#                           },
#   <authorization code #1> => {...},
#   ...
# }
# A User session dummy class. We probably want to use
# a KV-store at some point
class RequestCache
  @request_cache = {}
  def self.get
    @request_cache
  end
end

# Initialize admin user if given in ENV
if ENV['OMEJDN_ADMIN']
  admin_name, admin_pw = ENV['OMEJDN_ADMIN'].split(':')
  p "Setting admin username `#{admin_name}' and password `#{admin_pw}'" if debug
  admin = User.find_by_id(admin_name)
  if admin.nil?
    admin = User.new
    admin.username = admin_name
    admin.attributes = [{ 'key' => 'omejdn:api', 'value' => true }, { 'key' => 'omejdn:admin', 'value' => true },
                        { 'key' => 'name', 'value' => 'Admin' }]
    admin.password = BCrypt::Password.create(admin_pw)
    User.add_user(admin, 'yaml')
  else
    admin.password = BCrypt::Password.create(admin_pw)
    User.update_user(admin)
  end
end

before do
  return if request.get_header('HTTP_ORIGIN').nil?
  unless request.get_header('HTTP_ORIGIN').start_with?('chrome-extension://') ||
         request.get_header('HTTP_ORIGIN').start_with?('moz-extension://')
    return
  end

  response.headers['Access-Control-Allow-Origin'] = request.get_header('HTTP_ORIGIN').to_s
end

# Handle token request
post '/token' do
  client = nil
  scopes = []
  if params[:grant_type] == 'client_credentials'
    if params[:client_assertion_type] != 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer'
      halt 400, OAuthHelper.error_response('invalid_request', 'Invalid client_assertion_type')
    end
    jwt = params[:client_assertion]
    halt 400, OAuthHelper.error_response('invalid_client', 'Assertion missing') if jwt.nil?
    client = Client.find_by_jwt jwt
    halt 400, OAuthHelper.error_response('invalid_client', 'Client unknown') if client.nil?
  elsif (params[:grant_type] == 'authorization_code') && Config.base_config['openid']
    code = params[:code]
    # Only verify PKCE if given in request
    unless RequestCache.get[code][:pkce].nil?
      halt 400, OAuthHelper.error_response('invalid_request', 'Code verifier missing') if params[:code_verifier].nil?
      unless OAuthHelper.validate_pkce(RequestCache.get[code][:pkce],
                                       params[:code_verifier],
                                       RequestCache.get[code][:pkce_method])
        halt 400,
             OAuthHelper.error_response('invalid_request', 'Code verifier mismatch')
      end
    end
    client = Client.find_by_id params[:client_id]
    halt 400, OAuthHelper.error_response('invalid_client', 'No client_id given') if client.nil?
    halt 400, OAuthHelper.error_response('invalid_code', '') if code.nil?
    halt 400, OAuthHelper.error_response('invalid_code', '') unless RequestCache.get.keys.include?(code)
    scopes = RequestCache.get[code][:scopes] unless RequestCache.get[code][:scopes].nil?
  else
    halt 400, OAuthHelper.error_response('unsupported_grant_type', "Given: #{params[:grant_type]}")
  end
  headers['Content-Type'] = 'application/json'
  scopes = params[:scope]&.split if scopes.empty?
  # FIXME: filter scopes! Clients that are not authorized must be notified.
  id_token_claims = {}
  if !RequestCache.get[code].nil? &&
     RequestCache.get[code][:claims].key?('id_token') &&
     !RequestCache.get[code][:claims].empty?
    id_token_claims = RequestCache.get[code][:claims]['id_token']
  end
  begin
    user = nil
    user = RequestCache.get[code][:user] unless RequestCache.get[code].nil?
    # https://tools.ietf.org/html/draft-bertocci-oauth-access-token-jwt-00#section-2.2
    access_token = TokenHelper.build_access_token client, scopes, user
    if scopes.include?('openid')
      id_token = TokenHelper.build_id_token client, user,
                                            RequestCache.get[code][:nonce],
                                            id_token_claims, scopes
    end
    # Delete the authorization code as it is single use
    RequestCache.get.delete(code)
    OAuthHelper.token_response access_token, scopes, id_token
  rescue OAuth2Error
    halt 400, OAuthHelper.error_response('invalid_scope', '')
  end
end

get '/.well-known/openid-configuration' do
  headers['Content-Type'] = 'application/json'
  p "Host #{host},#{my_path}"
  JSON.generate OAuthHelper.openid_configuration(host, my_path)
end

# Handle authorization request
get '/authorize' do
  unless Config.base_config['openid']
    status 404
    return
  end
  session[:url_params] = params
  redirect to("#{my_path}/login") if session['user'].nil?
  user = nil
  unless params[:response_type] == 'code'
    return OAuthHelper.error_response 'unsupported_response_type', "Given: #{params[:response_type]}"
  end

  user = UserSession.get[session['user']]
  return OAuthHelper.error_response 'invalid_user', '' if user.nil?

  session[:scopes] = []
  scope_mapping = Config.scope_mapping_config

  params[:scope].split.each do |s|
    p "Checking scope #{s}"
    has_scope = false
    session[:scopes].push(s) if s == 'openid'
    next if scope_mapping[s].nil?

    scope_mapping[s].each do |claim|
      if user.claim?(claim)
        has_scope = true
        break
      end
    end
    session[:scopes].push(s) if has_scope
  end
  p "Granted scopes: #{session[:scopes]}"
  p "The user seems to be #{user.username}" if debug
  client = Client.find_by_id params['client_id']
  return OAuthHelper.error_response 'invalid_client' if client.nil?

  escaped_redir = CGI.unescape(params[:redirect_uri].gsub('%20', '+'))
  return OAuthHelper.error_response 'invalid_redirect_uri', '' unless [client.redirect_uri, 'localhost'].any? do |uri|
                                                                        escaped_redir.include? uri
                                                                      end

  # Seems to be in order
  return haml :authorization_page, locals: {
    user: user,
    client: client,
    host: my_path,
    scopes: session[:scopes],
    scope_description: Config.scope_description_config
  }
end

post '/authorize' do
  code = OAuthHelper.new_authz_code
  RequestCache.get[code] = {}
  RequestCache.get[code][:user] = UserSession.get[session['user']]
  RequestCache.get[code][:scopes] = session[:scopes]
  RequestCache.get[code][:nonce] = session[:url_params][:nonce]
  RequestCache.get[code][:claims] = {}
  RequestCache.get[code][:claims] = JSON.parse session[:url_params]['claims'] if session[:url_params].key?('claims')
  unless session[:url_params][:code_challenge].nil?
    unless session[:url_params][:code_challenge_method] == 'S256'
      return OAuthHelper.error_response 'invalid_request',
                                        'Transform algorithm not supported'
    end

    RequestCache.get[code][:pkce] = session[:url_params][:code_challenge]
    RequestCache.get[code][:pkce_method] = session[:url_params][:code_challenge_method]
  end
  redirect_uri = session[:url_params][:redirect_uri]
  resp = "?code=#{code}&state=#{session[:url_params][:state]}"
  redirect to(redirect_uri + resp)
end

get '/.well-known/jwks.json' do
  headers['Content-Type'] = 'application/json'
  OAuthHelper.generate_jwks.to_json
end

before '/userinfo' do
  @user = nil
  return if request.env['REQUEST_METHOD'] == 'OPTIONS'

  jwt = env.fetch('HTTP_AUTHORIZATION', '').slice(7..-1)
  halt 401 if jwt.nil? || jwt.empty?
  begin
    key = Server.load_key
    @token = JWT.decode jwt, key.public_key, true, { algorithm: 'RS256' }
    @user = User.find_by_id(@token[0]['sub'])
  rescue StandardError => e
    p e if debug
    @user = nil
  end
  halt 401 if @user.nil?
end

get '/userinfo' do
  headers['Content-Type'] = 'application/json'
  # JSON.generate OAuthHelper.access_token_to_userinfo(@token)
  JSON.generate OAuthHelper.userinfo(@user, @token)
end

########## LOGIN/LOGOUT ##################

get '/logout' do
  session['user'] = nil
  redirect to("#{my_path}/login")
end

post '/logout' do
  redirect_uri = session['post_logout_redirect_uri']
  redirect to(redirect_uri)
end

# FIXME
# This should use a more generic way to select the OP to use
get '/login' do
  config = Config.oauth_provider_config
  providers = []
  unless config == false
    config&.each do |provider|
      url = URI(provider['authorization_endpoint'])
      params = { client_id: provider['client_id'], scope: provider['scopes'],
                 redirect_uri: provider['redirect_uri'], response_type: provider['response_type'] }
      url.query = URI.encode_www_form(params)
      providers.push({ url: url.to_s, name: provider['name'], logo: provider['logo'] })
    end
  end
  no_password_login = Config.base_config['no_password_login']
  no_password_login = false if no_password_login.nil?
  return haml :login, locals: {
    no_password_login: no_password_login,
    host: my_path,
    providers: providers
  }
end

post '/login' do
  user = User.find_by_id(params[:username])
  redirect to("#{my_path}/login?error=\"Not a valid user.\"") if user.nil?
  redirect to("#{my_path}/login?error=\"Credentials incorrect\"") unless User.verify_credential(user,
                                                                                                params[:password])
  nonce = rand(2**512)
  UserSession.get[nonce] = user
  session['user'] = nonce
  if session[:url_params].nil?
    redirect to("#{my_path}/login")
  else
    redirect to("#{my_path}/authorize?#{URI.encode_www_form(session[:url_params]).gsub('+', '%20')}")
  end
end

# FIXME
# This should also be more generic and use the correct OP
get '/oauth_cb' do
  oauth_providers = Config.oauth_provider_config
  code = params[:code]
  at = nil
  provider_index = 0
  oauth_providers.each do |provider|
    break if provider['name'] == params[:provider]

    provider_index += 1
  end
  uri = URI(oauth_providers[provider_index]['token_endpoint'])
  Net::HTTP.start(uri.host, uri.port, use_ssl: true) do |http|
    req = Net::HTTP::Post.new(uri)
    req.set_form_data('code' => code,
                      'client_id' => oauth_providers[provider_index]['client_id'],
                      'client_secret' => oauth_providers[provider_index]['client_secret'],
                      'grant_type' => 'authorization_code',
                      'redirect_uri' => oauth_providers[provider_index]['redirect_uri'])
    res = http.request req
    at = JSON.parse(res.body)['access_token']
  end
  return 'Unauthorized' if at.nil?

  user = nil
  nonce = rand(2**512)
  uri = URI(oauth_providers[provider_index]['userinfo_endpoint'])
  Net::HTTP.start(uri.host, uri.port, use_ssl: true) do |http|
    req = Net::HTTP::Get.new(uri)
    req['Authorization'] = "Bearer #{at}"
    res = http.request req
    user = User.generate_extern_user(oauth_providers[provider_index], JSON.parse(res.body))
  end
  return 'Internal Error' if user.username.nil?

  UserSession.get[nonce] = user
  session['user'] = nonce
  redirect to(my_path) if session[:url_params].nil? # This is actually an error
  redirect to("#{my_path}/authorize?#{URI.encode_www_form(session[:url_params])}")
end

########## User Selfservice ##########

before '/api/v1/user/*' do
  return if request.env['REQUEST_METHOD'] == 'OPTIONS'

  jwt = env.fetch('HTTP_AUTHORIZATION', '').slice(7..-1)
  halt 401 if jwt.nil? || jwt.empty?
  @user_is_admin = false
  begin
    key = Server.load_key
    token = JWT.decode(jwt, key.public_key, true, { algorithm: 'RS256' })
    halt 401 unless token[0]['scopes'].include? 'omejdn:api'
    @user = User.find_by_id token[0]['sub'] if token[0]['scopes'].include? 'openid'
    @client = Client.find_by_id token[0]['sub'] unless (token[0]['scopes']).include? 'openid'
    @user_is_admin = token[0]['scopes'].include? 'omejdn:admin'
  rescue StandardError => e
    p e if debug
    @client = nil
    @user = nil
  end
  halt 401 if @client.nil? && @user.nil?
end

put '/api/v1/user/:username/password' do
  user = User.find_by_id(params['username'])
  halt 401 unless (@user.username == user.username) || @user_is_admin
  json = (JSON.parse request.body.read)
  current_password = json['currentPassword']
  new_password = json['newPassword']
  unless User.verify_credential(user, current_password)
    halt 403, { 'passwordChange' => 'not successfull, password incorrect' }
  end
  User.change_password(user, new_password)
  halt 204
end

get '/api/v1/user/:username' do
  halt 401 unless (@user.username == params['username']) || @user_is_admin
  Config.all_users.each do |key|
    return JSON.generate key if key['username'].eql?(params['username'])
  end
  halt 404
end

post '/api/v1/user/:username' do
  halt 401 unless (@user.username == params['username']) || @user_is_admin
  json = JSON.parse request.body.read
  user = User.from_json(json)
  User.add_user(user, json['userBackend'])
  halt 201
end

put '/api/v1/user/:username' do
  halt 401 unless (@user.username == params['username']) || @user_is_admin
  user = User.from_json(JSON.parse(request.body.read))
  oauth_providers = Config.oauth_provider_config
  User.update_user(user, oauth_providers)
  halt 204
end

delete '/api/v1/user/:username' do
  halt 401 unless (@user.username == params['username']) || @user_is_admin
  User.delete_user(params['username'])
  halt 204
end

get '/api/v1/user/:username/provider/:provider' do
  halt 401 unless (@user.username == params['username']) || @user_is_admin
  halt 401 unless @user.extern == params['provider']
  providers = Config.oauth_provider_config
  providers.each do |provider|
    next unless provider['name'] == params['provider']

    return JSON.generate provider
  end
  halt 404
end

########## ADMIN API ##################

before '/api/v1/config/*' do
  return if request.env['REQUEST_METHOD'] == 'OPTIONS'

  jwt = env.fetch('HTTP_AUTHORIZATION', '').slice(7..-1)
  halt 401 if jwt.nil? || jwt.empty?
  begin
    key = Server.load_key
    token = JWT.decode(jwt, key.public_key, true, { algorithm: 'RS256' })
    halt 401 unless token[0]['scopes'].include? 'omejdn:api'
    halt 401 unless token[0]['scopes'].include? 'omejdn:admin'
    @user = User.find_by_id token[0]['sub'] if token[0]['scopes'].include? 'openid'
    @client = Client.find_by_id token[0]['sub'] unless (token[0]['scopes']).include? 'openid'
  rescue StandardError => e
    p e if debug
    @client = nil
    @user = nil
  end
  halt 401 if @client.nil? && @user.nil?
end

after '/api/v1/*' do
  headers['Content-Type'] = 'application/json'
end

get '/api/v1/config/omejdn' do
  JSON.generate Config.base_config
end

put '/api/v1/config/omejdn' do
  Config.base_config = JSON.parse request.body.read
  halt 204
end

post '/api/v1/config/omejdn' do
  begin
    setting = JSON.parse request.body.read
    Config.base_config = setting
  rescue StandardError
    halt 400
  end
  halt 201
end

get '/api/v1/config/users' do
  JSON.generate Config.all_users
end

get '/api/v1/config/users/:username' do
  Config.all_users.each do |key|
    return JSON.generate key if key['username'].eql?(params['username'])
  end
  halt 404
end

post '/api/v1/config/users/:username' do
  json = JSON.parse request.body.read
  user = User.from_json(json)
  User.add_user(user, json['userBackend'])
  halt 201
end

put '/api/v1/config/users/:username' do
  user = User.from_json(JSON.parse(request.body.read))
  oauth_providers = Config.oauth_provider_config
  User.update_user(user, oauth_providers)
  halt 204
end

delete '/api/v1/config/users/:username' do
  User.delete_user(params['username'])
  halt 204
end

put '/api/v1/config/users/password/:username' do
  user = User.find_by_id(params[:username])
  json = (JSON.parse request.body.read)
  current_password = json['currentPassword']
  new_password = json['newPassword']
  unless User.verify_credential(user, current_password)
    halt 403, { 'passwordChange' => 'not successfull, password incorrect' }
  end
  User.change_password(user, new_password)
  halt 204
end

get '/api/v1/config/clients' do
  JSON.generate Config.client_config
end

get '/api/v1/config/clients/:client_id' do
  Config.client_config.each do |key|
    next unless key['client_id'].eql?(params['client_id'])

    halt 200, JSON.generate(key)
  end
  halt 404
end

post '/api/v1/config/clients' do
  Config.client_config = JSON.parse request.body.read
  halt 201
end

post '/api/v1/config/clients/:client_id' do
  client = Client.from_json(JSON.parse(request.body.read))
  clients = Client.load_clients
  clients << client
  Config.client_config = clients
  halt 201, client.to_json
end

put '/api/v1/config/clients/:client_id' do
  client = Client.from_json(JSON.parse(request.body.read))
  clients = Client.load_clients
  clients.each do |stored_client|
    next if stored_client.client_id != client.client_id

    stored_client.attributes = client.attributes
    stored_client.allowed_scopes = client.allowed_scopes
    stored_client.redirect_uri = client.redirect_uri
    stored_client.certificate = client.certificate
    Config.client_config = clients
    halt 200, stored_client.to_json
  end
  halt 404
end

delete '/api/v1/config/clients/:client_id' do
  clients = Client.load_clients
  clients.each do |stored_client|
    next unless stored_client.client_id.eql?(params['client_id'])

    clients.delete(stored_client)
    Config.client_config = clients
    halt 204
  end
  halt 404
end

get '/api/v1/config/clients/keys/:client_id' do
  clients = Client.load_clients
  clients.each do |stored_client|
    next unless stored_client.client_id.eql?(params['client_id'])

    return JSON.generate({ 'certfile' => stored_client.certificate_file,
                           'certificate' => stored_client.certificate.to_s })
  end
  halt 404
end

put '/api/v1/config/clients/keys/:client_id' do
  cert = JSON.parse(request.body.read)
  clients = Client.load_clients
  clients.each do |stored_client|
    next unless stored_client.client_id.eql?(params['client_id'])

    stored_client.certificate = cert['certificate']
    Config.client_config = clients
    halt 200, JSON.generate({ 'certfile' => stored_client.certificate_file,
                              'certificate' => stored_client.certificate.to_s })
  end
  halt 404
end

post '/api/v1/config/clients/keys/:client_id' do
  cert = JSON.parse(request.body.read)
  clients = Client.load_clients
  clients.each do |stored_client|
    next unless stored_client.client_id.eql?(params['client_id'])

    stored_client.certificate = cert['certificate']
    Config.client_config = clients
    halt 201, JSON.generate({ 'certfile' => stored_client.certificate_file,
                              'certificate' => stored_client.certificate.to_s })
  end
  halt 404
end

delete '/api/v1/config/clients/keys/:client_id' do
  clients = Client.load_clients
  clients.each do |stored_client|
    if stored_client.client_id.eql?(params['client_id'])
      stored_client.deleteCert
      halt 200
    end
  end
  halt 404
end

get '/api/v1/config/user_backend' do
  JSON.generate Config.user_backend_config
end

put '/api/v1/config/user_backend' do
  Config.user_backend_config = JSON.parse request.body.read
  halt 200
end

get '/api/v1/config/oauth_providers' do
  JSON.generate Config.oauth_provider_config
end

get '/api/v1/config/oauth_providers/:provider' do
  providers = Config.oauth_provider_config
  providers.each do |provider|
    next unless provider['name'] == params['provider']

    return JSON.generate provider
  end
  halt 404
end

post '/api/v1/config/oauth_providers/:provider' do
  new_provider = JSON.parse request.body.read
  providers = Config.oauth_provider_config
  providers.push(new_provider)
  Config.oauth_provider_config = providers
  halt 201
end

put '/api/v1/config/oauth_providers/:provider' do
  updated_provider = JSON.parse request.body.read
  providers = Config.oauth_provider_config
  providers.each do |provider|
    next unless provider['name'] == updated_provider['name']

    providers[providers.index(provider)] = updated_provider
    Config.oauth_provider_config = providers
    halt 200
  end
  halt 404
end

delete '/api/v1/config/oauth_providers/:provider' do
  providers = Config.oauth_provider_config
  providers.each do |provider|
    next unless provider['name'] == params['provider']

    providers.delete(provider)
    Config.oauth_provider_config = providers
    halt 200
  end
  halt 404
end

get '/api/v1/config/webfinger' do
  JSON.generate Config.webfinger_config
  halt 200
end

put '/api/v1/config/webfinger' do
  Config.webfinger_config = JSON.parse request.body.read
  halt 200
end

get '/.well-known/webfinger' do
  halt 400 if params[:resource].nil?

  res = CGI.unescape(params[:resource].gsub('%20', '+'))
  halt 400 unless res.start_with? 'acct:'

  email = res[5..-1]
  YAML.load_file('config/webfinger.yml').each do |wfhost, _|
    next unless email.end_with? "@#{wfhost}"

    return JSON.generate(
      {
        subject: "acct:#{email}",
        properties: {},
        links: [
          {
            rel: 'http://openid.net/specs/connect/1.0/issuer',
            href: my_path
          }
        ]
      }
    )
  end
  halt 404
end

get '/about' do
  headers['Content-Type'] = 'application/json'
  return JSON.generate({ 'version' => OMEJDN_VERSION,
                         'license' => OMEJDN_LICENSE })
end
