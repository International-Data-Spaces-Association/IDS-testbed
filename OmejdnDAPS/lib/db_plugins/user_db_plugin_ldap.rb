# frozen_string_literal: true

require 'socket'
require 'net/ldap'
require 'base64'

# LDAP User DB backend
class LdapUserDb < UserDb
  @dn_cache = {}

  def decode_value(value)
    begin
      ret = Base64.strict_decode64(value).force_encoding('utf-8')
      puts "Decoded to #{ret}"
      return ret if ret.valid_encoding?
    rescue StandardError
      puts 'Failed to decode, interpret as plain text'
    end
    value
  end

  def decode_value_ascii(value)
    begin
      ret = Base64.strict_decode64(value).force_encoding('ascii')
      puts "Decoded to #{ret}"
      return ret if ret.valid_encoding?
    rescue StandardError
      puts 'Failed to decode, interpret as plain text'
    end
    value
  end

  def map_oidc_claim(user, key, value)
    decoded = decode_value(value)
    case key
    when :l
      user.attributes << { 'key' => 'locality',
                           'value' => decoded }
    when :postalcode
      user.attributes << { 'key' => 'postal_code',
                           'value' => decode_value_ascii(value) }
    when :street
      user.attributes << { 'key' => 'street_address',
                           'value' => decoded }
    when :sn
      user.attributes << { 'key' => 'family_name',
                           'value' => decoded }
    when :givenname
      user.attributes << { 'key' => 'given_name',
                           'value' => decoded }
    when :telephonenumber
      user.attributes += [
        { 'key' => 'phone_number',
          'value' => decode_value_ascii(value) },
        { 'key' => 'phone_number_verified',
          'value' => true }
      ]
    when :mail
      user.attributes += [
        { 'key' => 'email',
          'value' => decoded },
        { 'key' => 'email_verified',
          'value' => true }
      ]
    end
  end

  def ldap_entry_to_user(entry)
    user_backend_config = Config.user_backend_config
    user = User.new
    user.username = entry[user_backend_config['ldap']['uidKey']][0]
    user.extern = true
    user.password = nil
    user.backend = 'ldap'
    user.attributes = []
    entry.each do |key, value|
      map_oidc_claim(user, key, value[0])
    end
    p user
    user
  end

  def load_users
    user_backend_config = Config.user_backend_config
    ldap = Net::LDAP.new
    ldap.host = user_backend_config['ldap']['host']
    ldap.port = user_backend_config['ldap']['port']
    base_dn = user_backend_config['ldap']['baseDN']
    t_users = []
    ldap.search(base: base_dn) do |entry|
      puts "DN: #{entry.dn}"
      user = ldap_entry_to_user(entry)
      t_users << user unless user.nil?
    end
    t_users
  end

  def users
    t_users = []
    load_users.each do |arr|
      user = User.new
      user.username = arr['username']
      user.extern = arr['extern']
      user.password = BCrypt::Password.new(arr['password']) unless user.extern
      user.attributes = arr['attributes']
      t_users << user
    end
    t_users
  end

  def bind(config, bdn, password)
    ldap = Net::LDAP.new
    ldap.host = config['ldap']['host']
    ldap.port = config['ldap']['port']
    puts "Trying bind for #{bdn}"
    ldap = Net::LDAP.new({
                           host: config['ldap']['host'],
                           port: config['ldap']['port'],
                           auth: {
                             method: :simple,
                             username: bdn,
                             password: password
                           },
                           encryption: {
                             method: :simple_tls,
                             tls_options: OpenSSL::SSL::SSLContext::DEFAULT_PARAMS
                           }
                         })
    return nil unless ldap.bind

    ldap
  end

  def nobind(config)
    Net::LDAP.new({
                    host: config['ldap']['host'],
                    port: config['ldap']['port'],
                    base: config['ldap']['base_dn'],
                    verbose: true,
                    encryption: {
                      method: :simple_tls,
                      tls_options: OpenSSL::SSL::SSLContext::DEFAULT_PARAMS
                    }
                  })
  end

  def lookup_user(user, config)
    return @dn_cache[user.username] unless @dnCache[user.username].nil?

    connect(config).search(base: config['ldap']['baseDN'],
                           filter: Net::LDAP::Filter.eq(config['ldap']['uidKey'],
                                                        user.username)) do |entry|
      return entry.dn
    end
    nil
  end

  def verify_credential(user, password)
    user_backend_config = Config.user_backend_config
    user_dn = @dn_cache[user.username]
    user_dn = lookup_user(user, user_backend_config) if user_dn.nil?

    return false if user_dn.nil?

    puts "Trying bind for #{user_dn}"
    Net::LDAP.new({
                    host: user_backend_config['ldap']['host'],
                    port: user_backend_config['ldap']['port'],
                    auth: {
                      method: :simple,
                      username: user_dn,
                      password: password
                    },
                    encryption: {
                      method: :simple_tls,
                      tls_options: OpenSSL::SSL::SSLContext::DEFAULT_PARAMS
                    }
                  }).bind
  end

  def connect(config)
    if ENV['OMEJDN_LDAP_BIND_DN'].nil? || ENV['OMEJDN_LDAP_BIND_PW'].nil?
      nobind(config)
    else
      bind(config, ENV['OMEJDN_LDAP_BIND_DN'], ENV['OMEJDN_LDAP_BIND_PW'])
    end
  end

  def find_by_id(username)
    user_backend_config = Config.user_backend_config
    ldap = connect(user_backend_config)
    p ldap
    base_dn = user_backend_config['ldap']['baseDN']
    uid_key = user_backend_config['ldap']['uidKey']
    puts "Looking for #{uid_key}=#{username}"
    filter = Net::LDAP::Filter.eq(uid_key, username)
    ldap.search(verbose: true, base: base_dn, filter: filter) do |entry|
      puts "DN: #{entry.dn}"
      @dn_cache[username] = entry.dn
      return ldap_entry_to_user(entry)
    end
    nil
  end
end

# Monkey patch the loader
class UserDbLoader
  def self.load_ldap_db
    LdapUserDb.new
  end
end
