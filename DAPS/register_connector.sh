#!/bin/sh

if [ ! $# -ge 1 ] || [ ! $# -le 3 ]; then
    echo "Usage: $0 NAME (SECURITY_PROFILE) (CERTFILE)"
    exit 1
fi

CLIENT_NAME=$1

CLIENT_SECURITY_PROFILE=$2
[ -z "$CLIENT_SECURITY_PROFILE" ] && CLIENT_SECURITY_PROFILE="idsc:BASE_SECURITY_PROFILE"

CLIENT_CERT="keys/$CLIENT_NAME.cert"
if [ -n "$3" ]; then
    [ ! -f "$3" ] && (echo "Cert not found"; exit 1)
    cert_format="DER"
    openssl x509 -noout -in "$3" 2>/dev/null && cert_format="PEM"
    openssl x509 -inform "$cert_format" -in "$3" -text > "$CLIENT_CERT"
else
    openssl req -newkey rsa:2048 -new -batch -nodes -x509 -days 3650 -text -keyout "keys/${CLIENT_NAME}.key" -out "$CLIENT_CERT"
fi

SKI="$(grep -A1 "Subject Key Identifier"  "$CLIENT_CERT" | tail -n 1 | tr -d ' ')"
AKI="$(grep -A1 "Authority Key Identifier"  "$CLIENT_CERT" | tail -n 1 | tr -d ' ')"
CLIENT_ID="$SKI:$AKI"

CLIENT_CERT_SHA="$(openssl x509 -in "$CLIENT_CERT" -noout -sha256 -fingerprint | tr '[:upper:]' '[:lower:]' | tr -d : | sed 's/.*=//')"

cat >> config/clients.yml <<EOF
- client_id: $CLIENT_ID
  client_name: $CLIENT_NAME
  grant_types: client_credentials
  token_endpoint_auth_method: private_key_jwt
  scope: idsc:IDS_CONNECTOR_ATTRIBUTES_ALL
  attributes:
  - key: idsc
    value: IDS_CONNECTOR_ATTRIBUTES_ALL
  - key: securityProfile
    value: $CLIENT_SECURITY_PROFILE
  - key: referringConnector
    value: http://${CLIENT_NAME}.demo
  - key: "@type"
    value: ids:DatPayload
  - key: "@context"
    value: https://w3id.org/idsa/contexts/context.jsonld
  - key: transportCertsSha256
    value: $CLIENT_CERT_SHA
  import_certfile: $CLIENT_CERT
EOF
