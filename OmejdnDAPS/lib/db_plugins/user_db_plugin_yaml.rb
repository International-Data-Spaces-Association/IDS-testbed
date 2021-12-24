# frozen_string_literal: true

# The DB backend for yaml files
class YamlUserDb < UserDb
  def create_user(user)
    t_users = users
    t_users << user
    write_user_db t_users
  end

  def delete_user(username)
    t_users = users
    t_users.each do |concretuser|
      next unless concretuser.username.eql?(username)

      t_users.delete(concretuser)
      write_user_db t_users
      return true
    end
    false
  end

  def update_user(user)
    t_users = users
    user_in_yaml = false
    t_users.each do |concretuser|
      next if concretuser.username != user.username

      concretuser.attributes = user.attributes
      concretuser.password = user.password
      concretuser.backend = user.backend
      concretuser.extern = user.extern
      user_in_yaml = true
    end
    write_user_db t_users if user_in_yaml
    user_in_yaml
  end

  def load_users
    user_backend_config = Config.user_backend_config
    (YAML.safe_load File.read user_backend_config['yaml']['location']) || []
  end

  def write_user_db(users)
    users_yaml = []
    users.each do |user|
      users_yaml << user.to_dict
    end
    user_backend_config = Config.user_backend_config
    Config.write_config(user_backend_config['yaml']['location'], users_yaml.to_yaml)
  end

  def users
    t_users = []
    load_users.each do |arr|
      user = User.new
      user.username = arr['username']
      user.extern = arr['extern']
      user.password = BCrypt::Password.new(arr['password']) unless user.extern
      user.attributes = arr['attributes']
      user.backend = 'yaml'
      t_users << user
    rescue BCrypt::Errors::InvalidHash => e
      p "Error adding user: #{e}"
    end
    t_users
  end

  def change_password(user, password)
    t_users = users
    user_in_yaml = false
    _users.each do |concretuser|
      next if concretuser.username != user.username

      concretuser.password = password
      write_user_db t_users
      user_in_yaml = true
    end
    write_user_db t_users if user_in_yaml
    user_in_yaml
  end

  def verify_credential(user, password)
    user.password == password
  end

  def find_by_id(username)
    load_users.each do |arr|
      next unless arr['username'] == username

      user = User.new
      user.username = arr['username']
      user.extern = arr['extern']
      user.password = BCrypt::Password.new(arr['password']) unless user.extern
      user.attributes = arr['attributes']
      user.backend = 'yaml'
      return user
    end
    nil
  end
end

# Monkey patch the loader
class UserDbLoader
  def self.load_yaml_db
    YamlUserDb.new
  end
end
