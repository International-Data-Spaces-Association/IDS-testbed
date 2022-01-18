# frozen_string_literal: true

require_relative './config'
require 'openssl'

# Server setup helper functions.
class Server
  def self.setup_key(filename)
    rsa_key = OpenSSL::PKey::RSA.new 2048
    file = File.new filename, File::CREAT | File::TRUNC | File::RDWR
    file.write(rsa_key.to_pem)
    file.close
    p "Created new key at #{filename}"
  end

  def self.load_key(token_type = 'token')
    filename = Config.base_config[token_type]['signing_key']
    setup_key(filename) unless File.exist? filename
    OpenSSL::PKey::RSA.new File.read(filename)
  end
end
