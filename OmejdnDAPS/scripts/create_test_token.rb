# frozen_string_literal: true

require 'openssl'
require 'jwt'
require 'json'

##
# NOTE:
# The client_id in config/clients.yml must match the 'iss' and 'sub' claim
# of the JWT you generate.
# Do not forget to configure the 'certfile' of your client so that
# omejdn can find you public key which corrsponds to the private key you
# use to sign this JWT.
#
# The 'aud' claim MUST correspond to the HOST environment parameter
# or the 'host' value in the config/omejdn.yml.
# Alternatively, if omejdn is started with the OMEJDN_JWT_AUD_OVERRIDE
# environment variable you must use that value instead.
#

CLIENTID = 'testidsa9'

def load_key
  if File.exist? "#{CLIENTID}.key"
    filename = "#{CLIENTID}.key"
    rsa_key = OpenSSL::PKey::RSA.new File.read(filename)
  else
    rsa_key = OpenSSL::PKey::RSA.new 2048
    pfile = File.new "#{CLIENTID}.key", File::CREAT | File::TRUNC | File::RDWR
    pfile.write(rsa_key.to_pem)
    pfile.close
  end
  rsa_key
end

# Only for debugging!
client_rsa_key = load_key
payload = {
  'iss' => '66:07:ED:E5:80:E4:29:6D:1E:DD:F7:43:CA:0E:EB:38:32:C8:3A:43:keyid:07:FC:95:17:C4:95:B9:E4:AD:09:5F:07:1E:D2:20:75:2D:89:66:85',
  'sub' => '66:07:ED:E5:80:E4:29:6D:1E:DD:F7:43:CA:0E:EB:38:32:C8:3A:43:keyid:07:FC:95:17:C4:95:B9:E4:AD:09:5F:07:1E:D2:20:75:2D:89:66:85',
  'exp' => Time.new.to_i + 36000,
  'nbf' => Time.new.to_i,
  'iat' => Time.new.to_i,
  'aud' => 'idsc:IDS_CONNECTORS_ALL' # The omejdn host or OMEJDN_JWT_AUD_OVERRIDE value
}
token = JWT.encode payload, client_rsa_key, 'RS256'
puts token
