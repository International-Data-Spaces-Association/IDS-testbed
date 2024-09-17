#!/bin/sh

# Validate the number of arguments (1 to 3)
if [ "$#" -lt 1 ] || [ "$#" -gt 3 ]; then
  echo "Usage: $0 NAME (SECURITY_PROFILE) (CERTFILE)"
  exit 1
fi

CLIENT_NAME=$1
CLIENT_SECURITY_PROFILE=$2
[ -z "$CLIENT_SECURITY_PROFILE" ] && CLIENT_SECURITY_PROFILE="idsc:BASE_SECURITY_PROFILE"
CLIENT_CERT="keys/$CLIENT_NAME.cert"

# Extract SKI and AKI using openssl
SKI="$(openssl x509 -in "$CLIENT_CERT" -noout -text | grep -A1 "Subject Key Identifier" | tail -n 1 | tr -d ' ')"
AKI="$(openssl x509 -in "$CLIENT_CERT" -noout -text | grep -A1 "Authority Key Identifier" | tail -n 1 | tr -d ' ')"
SUB='keyid'

# Determine CLIENT_ID based on presence of 'keyid' in AKI
if echo "$AKI" | grep -q "$SUB"; then
  CLIENT_ID="$SKI:$AKI"
else
  CLIENT_ID="$SKI:keyid:$AKI"
fi

CLIENT_CERT_SHA="$(openssl x509 -in "$CLIENT_CERT" -noout -sha256 -fingerprint | tr '[:upper:]' '[:lower:]' | tr -d : | sed 's/.*=//')"

# Check if a client with the same client_id or client_name exists
CLIENT_EXISTS=$(yq eval '.[] | select(.client_id == "'"$CLIENT_ID"'" or .client_name == "'"$CLIENT_NAME"'")' config/clients.yml)

if [ -n "$CLIENT_EXISTS" ]; then
  echo "Client with ID $CLIENT_ID or name $CLIENT_NAME already exists. Updating the existing entry."

  # Update client_id, client_name, and transportCertsSha256 for the exact matching entry
  yq eval -i '
    map(
      select(.client_id == "'"$CLIENT_ID"'" or .client_name == "'"$CLIENT_NAME"'") |= 
      (.client_id = "'"$CLIENT_ID"'" | 
       .client_name = "'"$CLIENT_NAME"'" | 
       (.attributes[] | select(.key == "transportCertsSha256").value) = "'"$CLIENT_CERT_SHA"'")
    )
  ' config/clients.yml

  echo "Client entry updated successfully."
  exit 0
fi

# If the client does not exist, append the new client entry
echo "Adding new client entry to config/clients.yml."

# Ensure the file ends with a newline
if [ -n "$(tail -c 1 config/clients.yml)" ] && [ "$(tail -c 1 config/clients.yml)" != $'\n' ]; then
  echo >>config/clients.yml
fi

# Append the new client entry
yq eval -i '. += [{"client_id": "'"$CLIENT_ID"'", "client_name": "'"$CLIENT_NAME"'", "grant_types": "client_credentials", "token_endpoint_auth_method": "private_key_jwt", "scope": "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL", "attributes": [{"key": "idsc", "value": "IDS_CONNECTOR_ATTRIBUTES_ALL"}, {"key": "securityProfile", "value": "'"$CLIENT_SECURITY_PROFILE"'"}, {"key": "referringConnector", "value": "http://'"${CLIENT_NAME}"'.demo"}, {"key": "@type", "value": "ids:DatPayload"}, {"key": "@context", "value": "https://w3id.org/idsa/contexts/context.jsonld"}, {"key": "transportCertsSha256", "value": "'"$CLIENT_CERT_SHA"'"}], "import_certfile": "'"$CLIENT_CERT"'"}]' config/clients.yml

echo "Client entry added successfully."
