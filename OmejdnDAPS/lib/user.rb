# frozen_string_literal: true

require 'bcrypt'
require 'sinatra/activerecord'
require_relative './claim_mapper'
require_relative './user_db'

# Class representing a user from a DB
class User
  include BCrypt

  attr_accessor :username, :password, :attributes, :extern, :backend

  def self.verify_credential(user, pass)
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      next unless db.name == user.backend

      return db.verify_credential(user, pass)
    end
    puts 'User not found'
    false
  end

  def self.find_by_id(username)
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      user = db.find_by_id(username)
      return user unless user.nil?
    end
    puts 'User not found'
    nil
  end

  def self.from_json(json)
    user = User.new
    user.username = json['username']
    user.attributes = json['attributes']
    user.extern = json['extern']
    user.password = BCrypt::Password.create(json['password']) unless user.extern

    user
  end

  def to_dict
    {
      'username' => username,
      'attributes' => attributes,
      'password' => password.to_s,
      'extern' => extern
    }
  end

  def self.delete_user(username)
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      user_found = db.delete_user(username)
      break if user_found
    end
  end

  def self.add_user(user, user_backend)
    db = UserDbLoader.public_send("load_#{user_backend}_db")
    db.create_user(user)
  end

  def self.update_user(user, _oauth_providers = nil)
    # Extern User: Update omejdn:api scope if necessary
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      user_found = db.update_user(user)
      break if user_found
    end
  end

  def self.change_password(user, new_password)
    password = BCrypt::Password.create(new_password)
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      user_found = db.change_password(user, password)
      return true if user_found
    end
  end

  def self.generate_extern_user(provider, json)
    return nil if json[provider['external_userid']].nil?

    username = json[provider['external_userid']]
    user = User.find_by_id(username)
    return user unless user.nil?

    user = User.new
    user.username = username
    user.extern = provider['name'] || false
    user.attributes = []
    user.attributes |= ClaimMapper.map_claims(json, provider) unless provider['claim_mapper'].nil?
    User.add_user(user, 'yaml')
    user
  end

  def claim?(claim)
    attributes.each do |a|
      return true if a['key'] == claim
    end
    false
  end

  def remove_claim(claim)
    attributes.each do |a|
      attributes.delete a if a['key'] == claim
    end
  end

  def add_scopeclaim(claim)
    attributes.push({
                      'key' => claim,
                      'value' => true
                    })
  end
end
