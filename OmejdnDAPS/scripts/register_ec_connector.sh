#!/bin/sh

if [ ! $# -ge 2 ] || [ ! $# -le 3 ]; then
    echo "Usage: $0 NAME EC_BITS (SECURITY_PROFILE)"
    exit 1
fi

CLIENT_NAME=$1
CLIENT_CERT="keys/$CLIENT_NAME.cert"

if [ $2 = "521" ]; then
    openssl genpkey -genparam -algorithm ec -pkeyopt ec_paramgen_curve:P-521 -out EC521PARAM.key
    openssl req -newkey ec:EC521PARAM.key -new -batch -nodes -x509 -days 3650 -text -keyout "keys/${CLIENT_NAME}.key" -out "$CLIENT_CERT"
    rm EC521PARAM.key
else
    openssl genpkey -genparam -algorithm ec -pkeyopt ec_paramgen_curve:P-256 -out EC256PARAM.key
    openssl req -newkey ec:EC256PARAM.key -new -batch -nodes -x509 -days 3650 -text -keyout "keys/${CLIENT_NAME}.key" -out "$CLIENT_CERT"
    rm EC256PARAM.key
fi

CLIENT_SECURITY_PROFILE=$3
[ -z "$CLIENT_SECURITY_PROFILE" ] && CLIENT_SECURITY_PROFILE="idsc:BASE_SECURITY_PROFILE"

SKI="$(grep -A1 "Subject Key Identifier"  "$CLIENT_CERT" | tail -n 1 | tr -d ' ')"
AKI="$(grep -A1 "Authority Key Identifier"  "$CLIENT_CERT" | tail -n 1 | tr -d ' ')"
CLIENT_ID="$SKI:$AKI"

CLIENT_CERT_SHA="$(openssl x509 -in "$CLIENT_CERT" -noout -sha256 -fingerprint | tr '[:upper:]' '[:lower:]' | tr -d : | sed 's/.*=//')"

cat <<EOF
- client_id: $CLIENT_ID
  name: $CLIENT_NAME
  redirect_uri: 
  allowed_scopes:
  - idsc:IDS_CONNECTOR_ATTRIBUTES_ALL
  attributes:
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
  certfile: $CLIENT_NAME.cert
EOF