# Certificate Authority (CA)

## Description

This software is intended to be used as a mock CA for test environments. It does not offer any security guarantees and must not be used in productive environments.

Please, note that this document is only required for those end users who want to create and use different certificates for the deployment of IDS-testbed components.

## Scope

This software simulates a PKI with root CAs, intermediate CAs and device certificates, including at the device certificates the AIA (Authority Information Access) extension information.

## Generate certificates

At this step, all the neccessary certificates are generated for the use of IDS-testbed components.

```
./setup_PKI.sh {FOLDER_TO_BE_CREATED}
```

It could look something like this:

```
./setup_PKI.sh data-cfssl
```

### Revoke certificates

At this section, it is detailed the neccessary steps to manually revoke any of the previous step generated certificates.
For the already existent folder "data-cfssl", the following certificate is revoked "connectorArevoked".

The steps required to revoke a certificate are detailed at the following commands which need to be executed inside "data-cfssl" folder where the certificates are located.

```
cd data-cfssl
# Obtain certificate information to extract serial and AKI number identifiers.
cfssl certinfo -cert certs/connectorArevoked.pem
# Command to revoke the certificate
# cfssl revoke -db-config ocsp/sqlite_db_components.json -serial {SERIAL_NUMBER} -aki {AKI_CERTIFICATE} -reason={REASON}
# where AKI must be included as shown by certinfo without ":" and with all lowercase letters.
# It could look something like this for the connectorArevoked certificate
cfssl revoke -db-config ocsp/sqlite_db_components.json -serial "581921879588615033625472730240878974097738102962" -aki "c476d0aacd9379350feba7646090a46bb4384d33" -reason="superseded"
```

###  Refresh the OCSP server to include the changes

Open a terminal at "data-cfssl" folder and execute the following commands:

```
cfssl ocsprefresh -db-config ocsp/sqlite_db_components.json -ca subca/subca.pem -responder ocsp/ocsp_components.pem -responder-key ocsp/ocsp_components-key.pem

cfssl ocspdump -db-config ocsp/sqlite_db_components.json > ocsp/ocspdump_components.txt

cfssl ocspserve -port=8888 -responses=ocsp/ocspdump_components.txt  -loglevel=0
```

### Verify the changes against the OCSP server

The following commands performs a check against the OCSP server to verify the revocation status of a certificate.

```
openssl ocsp -issuer ocsp/ocsp_components.pem -issuer subca/subca.pem -no_nonce -cert certs/{CERTIFICATE}.pem -CAfile subca/subca.pem -text -url http://localhost:8888
```

It could look something like this:

```
openssl ocsp -issuer ocsp/ocsp_components.pem -issuer subca/subca.pem -no_nonce -cert certs/connectorArevoked.pem -CAfile subca/subca.pem -text -url http://localhost:8888
```

### Extra commands for device certificates

At this section it is included the neccessary commands in order to obtain the certificates extensions required by IDS-testbed components for their implementation.

```
cd CertificateAuthority/data-cfssl/certs
```

```
openssl pkcs12 -export -out connectorA.p12 -in connectorA.pem -inkey connectorA-key.pem -passout pass:password
openssl pkcs12 -in connectorA.p12 -clcerts -nokeys -out connectorA.crt -passin pass:password
openssl pkcs12 -in connectorA.p12 -out connectorA.cert -nokeys -nodes -passin pass:password

openssl pkcs12 -export -out connectorB.p12 -in connectorB.pem -inkey connectorB-key.pem -passout pass:password
openssl pkcs12 -in connectorB.p12 -clcerts -nokeys -out connectorB.crt -passin pass:password
openssl pkcs12 -in connectorB.p12 -out connectorB.cert -nokeys -nodes -passin pass:password

openssl pkcs12 -export -out daps.p12 -in daps.pem -inkey daps-key.pem -passout pass:password
openssl pkcs12 -in daps.p12 -clcerts -nokeys -out daps.crt -passin pass:password
openssl pkcs12 -in daps.p12 -out daps.cert -nokeys -nodes -passin pass:password

openssl pkcs12 -export -out broker.p12 -in broker.pem -inkey broker-key.pem -passout pass:password
openssl pkcs12 -in broker.p12 -clcerts -nokeys -out broker.crt -passin pass:password
openssl pkcs12 -in broker.p12 -out broker.cert -nokeys -nodes -passin pass:password

openssl pkcs12 -export -out connectorArevoked.p12 -in connectorArevoked.pem -inkey connectorArevoked-key.pem -passout pass:password
openssl pkcs12 -in connectorArevoked.p12 -clcerts -nokeys -out connectorArevoked.crt -passin pass:password
openssl pkcs12 -in connectorArevoked.p12 -out connectorArevoked.cert -nokeys -nodes -passin pass:password


cp connectorA-key.pem connectorA.key
cp connectorB-key.pem connectorB.key
cp daps-key.pem daps.key
cp broker-key.pem broker.key
cp connectorArevoked-key.pem connectorArevoked.key

chmod 664 broker.cert 
chmod 664 broker.p12 
chmod 664 broker.crt
chmod 664 broker.key
chmod 664 daps.cert
chmod 664 daps.crt
chmod 664 daps.key
chmod 664 daps.p12
chmod 664 connectorA.cert
chmod 664 connectorA.crt
chmod 664 connectorA.key
chmod 664 connectorA.p12
chmod 664 connectorB.cert
chmod 664 connectorB.crt
chmod 664 connectorB.key
chmod 664 connectorB.p12
chmod 664 connectorArevoked.cert
chmod 664 connectorArevoked.crt
chmod 664 connectorArevoked.key
chmod 664 connectorArevoked.p12
```

### Extra commands for CA certificate

```
cd CertificateAuthority/data-cfssl/ca
openssl pkcs12 -export -out ca.p12 -in ca.pem -inkey ca-key.pem -passout pass:password
openssl pkcs12 -in ca.p12 -clcerts -nokeys -out ca.crt -passin pass:password
openssl pkcs12 -in ca.p12 -out ca.cert -nokeys -nodes -passin pass:password
cp ca-key.pem ca.key
chmod 664 ca.cert
chmod 664 ca.crt
chmod 664 ca.key
chmod 664 ca.p12
```

### Extra commands for subCA certificate

```
cd CertificateAuthority/data-cfssl/subca
openssl pkcs12 -export -out subca.p12 -in subca.pem -inkey subca-key.pem -passout pass:password
openssl pkcs12 -in subca.p12 -clcerts -nokeys -out subca.crt -passin pass:password
openssl pkcs12 -in subca.p12 -out subca.cert -nokeys -nodes -passin pass:password
cp subca-key.pem subca.key
chmod 664 subca.cert
chmod 664 subca.crt
chmod 664 subca.key
chmod 664 subca.p12
```
