# This script can be run to regenerate the certificates for the IDS-Testbed in a specified folder

if [ "$#" != "1" ] ; then
	echo "Usage: ./setup_CA.sh <pki_dir>"
	exit 1
fi

PKIINPUT="$(dirname "$0")/pkiInput"
PKIDIR="$1"

printf "PKIINPUT is %s\n" "$PKIINPUT"
printf "PKIDIR is %s\n" "$PKIDIR"

if [ -d "$PKIDIR" ]; then
	echo "$PKIDIR already exists. Please remove the existing pki or define another folder."
	exit 1
fi

mkdir -p $PKIDIR

CADIR="$PKIDIR/ca"
SUBCADIR="$PKIDIR/subca"
OCSPDIR=$(readlink -f "$PKIDIR/ocsp")
COMPDIR="$PKIDIR/certs"
shift

mkdir -p $CADIR
mkdir -p $SUBCADIR
mkdir -p $OCSPDIR
mkdir -p $COMPDIR

# 1. Set up root CA (using ca.json to generate ca.pem and ca-key.pem)
cfssl gencert -initca "$PKIINPUT/ca.json" | cfssljson -bare "$CADIR/ca"

# 2. Set up an OCSP Server for the Root CA
# Setup the database based on the .sql file derived from the files in https://github.com/cloudflare/cfssl/tree/master/certdb/mysql/migrations/
cat "$PKIINPUT/certs_subcas.sql" | sqlite3 "$OCSPDIR/certdb_subcas.db"
echo "{\"driver\":\"sqlite3\",\"data_source\":\"$OCSPDIR/certdb_subcas.db\"}" > "$OCSPDIR/sqlite_db_subcas.json"

# Generate key/certificate for OCSP Signing
cfssl genkey "$PKIINPUT/ocsp_subcas.json" | cfssljson -bare "$OCSPDIR/ocsp_subcas"
cfssl sign -ca "$CADIR/ca.pem" --config "$PKIINPUT/ca-config.json" -profile ocsp -ca-key "$CADIR/ca-key.pem" "$OCSPDIR/ocsp_subcas.csr" | cfssljson -bare "$OCSPDIR/ocsp_subcas"

# 3. Set up the subCA (using sub_ca.json)
cfssl genkey "$PKIINPUT/subca.json" | cfssljson -bare "$SUBCADIR/subca" 
cfssl sign -ca "$CADIR/ca.pem" -ca-key "$CADIR/ca-key.pem" -db-config "$OCSPDIR/sqlite_db_subcas.json" --config "$PKIINPUT/ca-config.json" -profile intermediate  "$SUBCADIR/subca.csr" | cfssljson -bare "$SUBCADIR/subca"

# 4. Set up OCSP Server for the Sub CA
# Setup the database based on the .sql file derived from the files in https://github.com/cloudflare/cfssl/tree/master/certdb/mysql/migrations/
cat "$PKIINPUT/certs_components.sql" | sqlite3 "$OCSPDIR/certdb_components.db"
echo "{\"driver\":\"sqlite3\",\"data_source\":\"$OCSPDIR/certdb_components.db\"}" > "$OCSPDIR/sqlite_db_components.json"

# Generate key/certificate for OCSP Signing
cfssl genkey "$PKIINPUT/ocsp_components.json" | cfssljson -bare "$OCSPDIR/ocsp_components"
cfssl sign -ca "$SUBCADIR/subca.pem" --config "$PKIINPUT/ca-config.json" -profile ocsp -ca-key "$SUBCADIR/subca-key.pem" "$OCSPDIR/ocsp_components.csr" | cfssljson -bare "$OCSPDIR/ocsp_components"

# 5. Generate and sign certificates for components in the testbed
cfssl genkey "$PKIINPUT/connectorA.json" | cfssljson -bare "$COMPDIR/connectorA"
cfssl sign -ca "$SUBCADIR/subca.pem" -ca-key "$SUBCADIR/subca-key.pem" -db-config "$PKIDIR/ocsp/sqlite_db_components.json" --config "$PKIINPUT/ca-config.json"  -profile "component" "$COMPDIR/connectorA.csr" | cfssljson -bare "$COMPDIR/connectorA"
cfssl genkey "$PKIINPUT/connectorB.json" | cfssljson -bare "$COMPDIR/connectorB"
cfssl sign -ca "$SUBCADIR/subca.pem" -ca-key "$SUBCADIR/subca-key.pem"  -db-config "$PKIDIR/ocsp/sqlite_db_components.json" --config "$PKIINPUT/ca-config.json"  -profile "component" "$COMPDIR/connectorB.csr" | cfssljson -bare "$COMPDIR/connectorB"
cfssl genkey "$PKIINPUT/broker.json" | cfssljson -bare "$COMPDIR/broker"
cfssl sign -ca "$SUBCADIR/subca.pem" -ca-key "$SUBCADIR/subca-key.pem"  -db-config "$PKIDIR/ocsp/sqlite_db_components.json" --config "$PKIINPUT/ca-config.json"  -profile "component" "$COMPDIR/broker.csr" | cfssljson -bare "$COMPDIR/broker"
cfssl genkey "$PKIINPUT/daps.json" | cfssljson -bare "$COMPDIR/daps"
cfssl sign -ca "$SUBCADIR/subca.pem" -ca-key "$SUBCADIR/subca-key.pem"  -db-config "$PKIDIR/ocsp/sqlite_db_components.json" --config "$PKIINPUT/ca-config.json"  -profile "component" "$COMPDIR/daps.csr" | cfssljson -bare "$COMPDIR/daps"
cfssl genkey "$PKIINPUT/connectorArevoked.json" | cfssljson -bare "$COMPDIR/connectorArevoked"
cfssl sign -ca "$SUBCADIR/subca.pem" -ca-key "$SUBCADIR/subca-key.pem"  -db-config "$PKIDIR/ocsp/sqlite_db_components.json" --config "$PKIINPUT/ca-config.json"  -profile "component" "$COMPDIR/connectorArevoked.csr" | cfssljson -bare "$COMPDIR/connectorArevoked"

# 6. TODO: Revoke connectorA_revoked automatically
# manually it can be done with
# cfssl certinfo -cert $COMPDIR/connectorA_revoked.pem
# cfssl revoke -db-config ocsp/sqlite_db_components.json -serial "<serial>" -aki "<aki>" -reason="<reason>" (aki as numerical as shown by certinfo, aki in hex without ":" with all letters in lower case
# -> cfssl revoke -db-config data-cfssl/ocsp/sqlite_db_components.json -serial "684884757867154147174595483456917541554534961223" -aki "3a37c80b479b17cedd8da3a8a8ae518e0fcc2b46" -reason="superseded" for the connectorA_revoked certificate

# 7. Prepare the OCSP provider for components
cfssl ocsprefresh -db-config "$OCSPDIR/sqlite_db_components.json" -ca "$SUBCADIR/subca.pem" -responder "$OCSPDIR/ocsp_components.pem" -responder-key "$OCSPDIR/ocsp_components-key.pem"
cfssl ocspdump -db-config "$OCSPDIR/sqlite_db_components.json" >"$OCSPDIR/ocspdump_components.txt"
# Run the OCSP provider with: cfssl ocspserve -port=8888 -responses="$OCSPDIR/ocspdump_components.txt" -loglevel=0
# Query status of revoked certificate connectorA_revoked with: $ openssl ocsp -issuer data-cfssl/ocsp/ocsp_components.pem -issuer data-cfssl/subca/subca.pem -no_nonce -cert data-cfssl/certs/connectorA_revoked.pem -CAfile data-cfssl/ca/ca.pem -text -url http://localhost:8888


# 7. Prepare the OCSP provider for subCA
cfssl ocsprefresh -db-config "$OCSPDIR/sqlite_db_subcas.json" -ca "$CADIR/ca.pem" -responder "$OCSPDIR/ocsp_subcas.pem" -responder-key "$OCSPDIR/ocsp_subcas-key.pem"
cfssl ocspdump -db-config "$OCSPDIR/sqlite_db_subcas.json" >"$OCSPDIR/subcas_components.txt"
# Run the OCSP provider with: cfssl ocspserve -port=8887 -responses="$OCSPDIR/ocspdump_subcas.txt" -loglevel=0
