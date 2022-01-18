# frozen_string_literal: true

require_relative './config'
require_relative './token_helper'
require 'json'
require 'set'
require 'securerandom'
require 'base64'
require 'digest'

# The OAuth Exception class
class OAuthError < RuntimeError
  attr_reader :reason

  def initialize(reason)
    super
    @reason = reason
  end
end

# Helper functions for OAuth related tasks
class OAuthHelper
  def self.token_response(access_token, scopes, id_token)
    response = {}
    response['access_token'] = access_token
    response['id_token'] = id_token unless id_token.nil?
    response['expires_in'] = Config.base_config['token']['expiration']
    response['token_type'] = 'bearer'
    response['scope'] = scopes.join ' '
    JSON.generate response
  end

  def self.supported_scopes
    scopes = Set[]
    Config.client_config.each do |_client_id, arr|
      next if arr['scopes'].nil?

      arr['scopes'].each do |scope|
        scopes.add(scope)
      end
    end
    scopes.to_a
  end

  def self.userinfo(user, token)
    userinfo = {}
    userinfo['sub'] = user.username
    user.attributes.each do |attribute|
      token[0].each do |key, _|
        next unless attribute['key'] == key

        TokenHelper.add_jwt_claim(userinfo, key, attribute['value'])
      end
    end
    userinfo
  end

  def self.default_scopes
    scopes = []
    Config.scope_mapping_config.each do |mapping|
      scopes << mapping[0]
    end
    scopes
  end

  def self.error_response(error, desc = '')
    response = { 'error' => error, 'error_description' => desc }
    JSON.generate response
  end

  def self.new_authz_code
    Base64.urlsafe_encode64(rand(2**512).to_s)
  end

  def self.validate_pkce(code_challenge, code_verifier, method)
    raise unless method == 'S256'

    digest = Digest::SHA256.new
    digest << code_verifier
    expected_challenge = digest.base64digest.gsub('+', '-').gsub('/', '_').gsub('=', '')
    expected_challenge == code_challenge
  end

  def self.generate_jwks
    jwk = JSON::JWK.new(
      Server.load_key.public_key,
      kid: 'default'
    )
    JSON::JWK::Set.new jwk
  end

  def self.openid_configuration(host, path)
    base_config = Config.base_config
    metadata = {}
    metadata['issuer'] = base_config['token']['issuer']
    metadata['authorization_endpoint'] = "#{path}/authorize"
    metadata['token_endpoint'] = "#{path}/token"
    metadata['userinfo_endpoint'] = "#{path}/userinfo"
    metadata['jwks_uri'] = "#{host}/.well-known/jwks.json"
    # metadata["registration_endpoint"] = "#{host}/FIXME"
    metadata['scopes_supported'] = OAuthHelper.default_scopes
    metadata['response_types_supported'] = ['code']
    metadata['response_modes_supported'] = ['query'] # FIXME: we only do query atm no fragment
    metadata['grant_types_supported'] = ['authorization_code']
    metadata['id_token_signing_alg_values_supported'] = base_config['token']['algorithm']
    metadata
  end

  def self.verify_authorization_request(params)
    client = Client.find_by_id params['client_id']
    unless params[:response_type] == 'code'
      raise OAuthHelper.error_response 'unsupported_response_type',
                                       "Given: #{params[:response_type]}"
    end
    raise OAuthHelper.error_response 'invalid_client', '' if client.nil?
    unless client.redirect_uri ==
           CGI.unescape(params[:redirect_uri].gsub('%20', '+'))
      raise OAuthHelper.error_response 'invalid_redirect_uri', ''
    end
  end

  def self.handle_authorization_request(params)
    verify_authorization_request(params)

    # Seems to be in order
    haml :authorization_page, locals: {
      client: client,
      scopes: params[:scope]
    }
  end
end
