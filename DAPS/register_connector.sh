#!/bin/sh

if [ ! $# -ge 1 ] || [ ! $# -le 3 ]; then
    echo "Usage: $0 NAME (SECURITY_PROFILE) (CERTFILE)"
    exit 1
fi

CLIENT_NAME=$1

CLIENT_SECURITY_PROFILE=$2
[ -z "$CLIENT_SECURITY_PROFILE" ] && CLIENT_SECURITY_PROFILE="idsc:BASE_SECURITY_PROFILE"

CLIENT_CERT="keys/$CLIENT_NAME.cert"

SKI="$(openssl x509 -in "keys/${CLIENT_NAME}.cert" -noout -text | grep -A1 "Subject Key Identifier" | tail -n 1 | tr -d ' ')"
AKI="$(openssl x509 -in "keys/${CLIENT_NAME}.cert" -noout -text | grep -A1 "Authority Key Identifier" | tail -n 1 | tr -d ' ')"
SUB='keyid'

contains() {
    string="$AKI"
    substring="$SUB"
    if test "${string#*$substring}" != "$string"
    then
        CLIENT_ID="$SKI:$AKI"    # $substring is in $string
    else
        CLIENT_ID="$SKI:keyid:$AKI"    # $substring is not in $string
    fi
}

contains "$AKI" "$SUB"

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
