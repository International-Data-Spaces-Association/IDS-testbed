# frozen_string_literal: true

require_relative './config'
require 'abstraction'

# Abstract UserDb interface
class UserDb
  abstract
  attr_accessor :name

  def create_user(user)
    raise NotImplementedError
  end

  def delete_user(username)
    raise NotImplementedError
  end

  def update_user(user)
    raise NotImplementedError
  end

  def users
    raise NotImplementedError
  end

  def load_users
    raise NotImplementedError
  end

  def change_password(user, new_password)
    raise NotImplementedError
  end

  def verify_credential(user, password)
    raise NotImplementedError
  end

  def find_by_id(user)
    raise NotImplementedError
  end
end

# The loader class
class UserDbLoader
  def self.load_db
    config = Config.base_config
    plugins = []
    config['user_backend'].each do |plugin|
      db = public_send("load_#{plugin}_db")
      db.name = plugin
      plugins << db
    end
    plugins
  end
end

require 'require_all'
require_rel 'db_plugins'
