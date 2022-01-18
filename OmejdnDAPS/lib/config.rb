# frozen_string_literal: true

require 'yaml'

OMEJDN_BASE_CONFIG_FILE = 'config/omejdn.yml'
OMEJDN_USER_CONFIG_FILE = 'config/users.yml'
OMEJDN_CLIENT_CONFIG_FILE = 'config/clients.yml'
OMEJDN_USER_BACKEND_CONFIG = 'config/user_backend.yml'
OMEJDN_OAUTH_PROVIDER_CONFIG = 'config/oauth_providers.yml'
SCOPE_DESCRIPTION_CONFIG = 'config/scope_description.yml'
SCOPE_MAPPING_CONFIG = 'config/scope_mapping.yml'
WEBFINGER_CONFIG = 'config/webfinger.yml'

# Configuration helpers functions
class Config
  def self.write_config(file, data)
    file = File.new file, File::CREAT | File::TRUNC | File::RDWR
    file.write data
    file.close
  end

  # FIXME: Not sure why we put this here.
  # One reason is that we wanted to keep the User class plain
  def self.all_users
    users = []
    dbs = UserDbLoader.load_db
    dbs.each do |db|
      users += db.load_users
    end
    users
  end

  def self.client_config
    YAML.safe_load File.read OMEJDN_CLIENT_CONFIG_FILE
  end

  def self.client_config=(clients)
    clients_yaml = []
    clients.each do |client|
      clients_yaml << client.to_dict
    end
    write_config(OMEJDN_CLIENT_CONFIG_FILE, clients_yaml.to_yaml)
  end

  def self.base_config
    YAML.safe_load File.read OMEJDN_BASE_CONFIG_FILE
  end

  def self.base_config=(config)
    # Make sure those are integers
    config['token']['expiration'] = config['token']['expiration'].to_i
    config['id_token']['expiration'] = config['token']['expiration'].to_i
    write_config OMEJDN_BASE_CONFIG_FILE, config.to_yaml
  end

  def self.user_backend_config
    YAML.safe_load File.read OMEJDN_USER_BACKEND_CONFIG
  end

  def self.user_backend_config=(config)
    user_backend_yaml = {
      'admin' => {
        'location' => config['admin'] ['location']
      },
      'yaml' => {
        'location' => config['yaml'] ['location']
      },
      'sqlite' => {
        'location' => config['sqlite'] ['location']
      },
      'ldap' => {
        'host' => config['ldap'] ['host'],
        'port' => config['ldap'] ['port'],
        'treebase' => config['ldap'] ['treebase']
      }
    }
    write_config OMEJDN_USER_BACKEND_CONFIG, user_backend_yaml.to_yaml
  end

  def self.oauth_provider_config
    YAML.safe_load File.read OMEJDN_OAUTH_PROVIDER_CONFIG
  end

  def self.oauth_provider_config=(providers)
    write_config(OMEJDN_OAUTH_PROVIDER_CONFIG, providers.to_yaml)
  end

  def self.scope_description_config
    YAML.safe_load File.read SCOPE_DESCRIPTION_CONFIG
  end

  def self.scope_mapping_config
    YAML.safe_load File.read SCOPE_MAPPING_CONFIG
  end

  def self.webfinger_config
    YAML.safe_load File.read WEBFINGER_CONFIG
  end

  def self.webfinger_config=(config)
    write_config(WEBFINGER_CONFIG, config.to_yaml)
  end
end
