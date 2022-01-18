# frozen_string_literal: true

require 'sqlite3'

# The SQlite DB plugin for users
class SqliteUserDb < UserDb
  def create_user(user)
    user_backend_config = Config.user_backend_config
    db = SQLite3::Database.open user_backend_config['sqlite']['location']
    db.execute 'CREATE TABLE IF NOT EXISTS password(username TEXT PRIMARY KEY, password TEXT)'
    db.execute 'CREATE TABLE IF NOT EXISTS attributes(username TEXT, key TEXT, value TEXT, PRIMARY KEY (username, key))'
    db.execute 'INSERT INTO password(username, password) VALUES(?, ?)', user.username, user.password
    user.attributes.each do |attribute|
      db.execute 'INSERT INTO attributes (username, key, value) VALUES (?, ?, ?)', user.username, attribute['key'],
                 attribute['value']
    end
    db.close
  end

  def delete_user(username)
    user_backend_config = Config.user_backend_config
    db = SQLite3::Database.open user_backend_config['sqlite']['location']
    user_in_sqlite = (db.execute 'SELECT EXISTS(SELECT 1 FROM password WHERE username=?)', username)[0][0]
    return false unless user_in_sqlite == 1

    db.execute 'DELETE FROM password WHERE username=?', username
    db.execute 'DELETE FROM attributes WHERE username=?', username
    true
  end

  def delete_missing_attributes(user, db)
    db.results_as_hash = true
    (db.execute 'SELECT key, value FROM attributes WHERE username=?', user.username).each do |existing_attribute|
      next if user.attributes.any? do |a|
                a['key'] == existing_attribute['key']
              end

      db.execute 'DELETE FROM attributes WHERE username=? AND key=?', user.username,
                 existing_attribute['key']
    end
    true
  end

  def update_user(user)
    user_backend_config = Config.user_backend_config
    db = SQLite3::Database.open user_backend_config['sqlite']['location']
    user_in_sqlite = (db.execute 'SELECT EXISTS(SELECT 1 FROM password WHERE username=?)', user.username)[0][0]
    return false unless user_in_sqlite == 1

    db.results_as_hash = true
    delete_missing_attributes(user, db)
    user.attributes.each do |attribute|
      db.execute 'INSERT OR REPLACE INTO attributes (username, key, value) VALUES (?, ?, ?)', user.username,
                 attribute['key'], attribute['value']
    end
    db.close
    true
  end

  def verify_credential(user, password)
    user.password == password
  end

  def load_users
    user_backend_config = Config.user_backend_config
    db = SQLite3::Database.open user_backend_config['sqlite']['location']
    db.results_as_hash = true
    begin
      t_users = db.execute 'SELECT * FROM password'
      t_users.each do |user|
        user['attributes'] =
          db.execute 'SELECT key, value FROM attributes WHERE attributes.username = ?', user['username']
      end
      db.close
      t_users
    rescue StandardError => e
      p e
      db.close
      []
    end
  end

  def users
    t_users = []
    load_users.each do |arr|
      user = User.new
      user.username = arr['username']
      user.password = arr['password']
      user.attributes = arr['attributes']
      t_users << user
    end
    t_users
  end

  def change_password(user, password)
    user_backend_config = Config.user_backend_config
    db = SQLite3::Database.open user_backend_config['sqlite']['location']
    user_in_sqlite = (db.execute 'SELECT EXISTS(SELECT 1 FROM password WHERE username=?)', user.username)[0][0]
    return false unless user_in_sqlite == 1

    db.execute 'UPDATE password SET password=? WHERE username=?', password, user.username
  end

  def find_by_id(username)
    load_users.each do |arr|
      next unless arr['username'] == username

      user = User.new
      user.username = arr['username']
      user.extern = arr['extern']
      user.password = BCrypt::Password.new(arr['password']) unless user.extern
      user.attributes = arr['attributes']
      user.backend = 'sqlite'
      return user
    end
    nil
  end
end

# Monkey patch the loader
class UserDbLoader
  def self.load_sqlite_db
    SqliteUserDb.new
  end
end
