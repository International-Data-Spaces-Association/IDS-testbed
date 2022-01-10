# IDSA Reference Testbed Installation (CA-DAPS-DSC-MDB)

The installation and configuration process is explained below for each of the components. To further support this document, the links to the official installation guides will be linked.

The software required for the successful deployment of the testbed will also be mentioned.
Software and versions used for the testbed:
- OS: Ubuntu 20.04.1 LTS
- Docker: 19.03.8+
- Docker-compose: 1.25
- Java: 11
- Maven: 3.6.3
- Ruby: 2.7.0

## Execution modes
You may either run the preconfigured testbed offered in this repo or follow the instructions for the manual setup below to set it up on your own and possibly adjust it to your needs.

## Target View: Preconfigured testbed (not available yet)
Preconfigured setup that can be directly launched
[TODO SQS: explain how to start the preconfigured setup]

DAPS:  
* can be reached at http://localhost:4567
* needs to be preconfigured to know connector A, B and the Broker

Connectors:
* connector A
  * can be reached at https://localhost:8080
  * needs to be preconfigured with a self-description and offering a dataset ("hallo world")

* connector B
  * can be reached at https://localhost:8081
  * needs to be preconfigured with a self-description and offering a dataset ("goodbye world")

Broker:
* can be reached at https://localhost[:443]
* needs to be aware of connector A, connector B and store their self-descriptions

### Setting up requirements

Software and versions used for the testbed:
- OS: Ubuntu 20.04.1 LTS
- Docker: 19.03.8+
- Docker-compose: 1.25
- Java: 11
- Maven: 3.6.3
- Ruby: 2.7.0
- Python3

First, verify your ubuntu version

```
$lsb_release -a
```

the output should be similar to this

```
No LSB modules are available.
Distributor ID: Ubuntu
Description:    Ubuntu 20.04 LTS
Release:        20.04
Codename:       focal
```

Then update your system with

```
sudo apt-get update
sudo apt-get upgrade
```

#### docker and docker-compose

As we need to run different components at the same time, install docker and docker-compose
```
sudo apt-get install docker
sudo apt-get install docker-compose
```

verify install with

```
docker version
```

The output should look similar to

```
Client:
 Version:           20.10.7
 API version:       1.41
 Go version:        go1.13.8
 Git commit:        20.10.7-0ubuntu5~20.04.2
 Built:             Mon Nov  1 00:34:17 2021
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true
```

> In some environments, e.g. WSL2, you might have to start the docker daemon manually by calling `dockered` .


```
docker-compose version
```

The output should look similar to

```
Cdocker-compose version 1.27.4, build 40524192
docker-py version: 4.3.1
CPython version: 3.7.7
OpenSSL version: OpenSSL 1.1.0l  10 Sep 2019
```

#### Java and maven

Some components like the Data Space Connector require Java 11. Install it with

```
sudo apt install openjdk-11-jdk
```
verify install with

```
java -version
```

The output should look similar to

```
openjdk version "11.0.11" 2021-04-20
OpenJDK Runtime Environment (build 11.0.11+9-Ubuntu-0ubuntu2.20.04)
OpenJDK 64-Bit Server VM (build 11.0.11+9-Ubuntu-0ubuntu2.20.04, mixed mode, sharing)
```
> To avoid problems while building components you should set the `JAVA_HOME`environment variable on your system.
>
> HINT: you might want to check with ``jrunscript -e 'java.lang.System.out.println(java.lang.System.getProperty("java.home"));'``


To enable the build process of the Data Space connector we insall [maven](https://maven.apache.org/)

```
sudo apt-get install maven
```

verify install with

```
mvn -version
```

The output should look similar to

```
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 11.0.11, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.10.60.1-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```


#### Python

The CA script provided with the IDS testbed requires python. Install it with

```
sudo apt install python3
sudo apt install python3-openssl
```

#### Ruby

The Omejdn Daps runs on Ruby. Setup Ruby by calling

```
sudo apt install ruby
```

#### Other tools

Some additional tools that might be useful

```
sudo apt install curl

sudo gem install jwt
```

## Installation of the components

###

First, let us set up the network with

```
docker network create testbed
```

## Download the Testbed
```
git clone https://github.com/International-Data-Spaces-Association/IDS-testbed.git
```

# CERTIFICATE AUTHORITY
Move to right directory, and make the files executable:

```
cd Testbed/CertificateAuthority/
chmod a+x *.py
```

The official documentation will cover the scope, dependencies and usage of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/CA

## Continue here after the official documentation has been followed

The Certificate Authority provides {CERT_FILENAME}.crt and {CERT_FILENAME}.key formats. Keep in mind that other formats will be required for the different components. Those have to be created.

Now convert the generated certificates in `data/cert` using openssl to then use in the Connector and DAPS.

```
## navigate to the following directory data/cert
cd data/cert
ls
```
The output should look similar to
```
{CERT_FILENAME}.crt  {CERT_FILENAME}.key
```
Obtain a `.p12` file format from the current `.crt` and `.key` formats:
```
## .crt + .key -> .p12
openssl pkcs12 -export -out {CERT_FILENAME}.p12 -inkey {CERT_FILENAME}.key -in {CERT_FILENAME}.crt -passout pass:password
## .p12 -> .cert
openssl pkcs12 -in {CERT_FILENAME}.p12 -out {CERT_FILENAME}.cert -nokeys -nodes -passin pass:password

```

You should now have two additional files in data/cert

```
{CERT_FILENAME}.cert  {CERT_FILENAME}.crt  {CERT_FILENAME}.key  {CERT_FILENAME}.p12
```

Move the certificates to their respective components and directories. **TO BE MODIFIED WHEN ALL COMPONENTS ARE UPDATED**
```
## WAIT UNTIL THE REST OF THE COMPONENTS ARE OUT OF ZIP FILES TO CONFIRM
cp {CERT_FILENAME}.p12 ../../../DataspaceConnector/src/main/resources/conf
cp {CERT_FILENAME}.cert ../../../OmejdnDAPS/keys

```

The certificate chain (CA, SubCA, Certs) has been created and the user should be able to create as many certificates as they need for their environment.

# DAPS

The official documentation of the Omejdn DAPS is here: https://github.com/International-Data-Spaces-Association/omejdn-daps

## Adding the keys to the DAPS

Every client that wants to use the local Omejdn DAPS must place their `{CERTFILE}.cert` file in the `keys` directory.

The directory can be found in

```
OmejdnDAPS/keys
```

## Adding the clients to the DAPS

**Note:** The user must extract the aki/ski extensions from the client's certificate to add the client to the Omejdn DAPS.  If you are not certain on how to do this,  the `keys` directory will have a script called `extensions.sh`. Once executed, input the certificate's filename and it will return the required aki/ski extensions.

To execute the script

```
chmod +x extensions.sh
./extensions.sh
```

It could look something like this

```
chmod +x extensions.sh
./extensions.sh
> Input your certificate filename:
testbed1.cert
> The aki/ski extension for testbed1.cert is:
66:07:ED:E5:80:E4:29:6D:1E:DD:F7:43:CA:0E:EB:38:32:C8:3A:43:keyid:07:FC:95:17:C4:95:B9:E4:AD:09:5F:07:1E:D2:20:75:2D:89:66:85
```

Change the configuration with your favorite editor, e.g. `nano`.

```
nano config/clients.yml
```

Add the aki/ski extension from the client's certificate in `client_id`

It could look something like this 

```
client_id: 66:07:ED:E5:80:E4:29:6D:1E:DD:F7:43:CA:0E:EB:38:32:C8:3A:43:keyid:07:FC:95:17:C4:95:B9:E4:AD:09:5F:07:1E:D2:20:75:2D:89:66:85`
```
and change the `certfile` to the {CERTFILE}.cert file dropped earlier in the `keys` directory

It could look something like this 

```
certile: testbed1.cert
```

## Required changes in the configuration

Change the configuration with your favorite editor, e.g. `nano`.

```
nano config/omejdn.yml
```

Replace `host` and `audience` with `idsc:IDS_CONNECTORS_ALL`

Replace `issuer` in `token` and `id_token` with `http://omejdn:4567`

```
host: idsc:IDS_CONNECTORS_ALL
openid: true
token:
  expiration: 3600
  signing_key: signing_key.pem
  algorithm: RS256
  audience: idsc:IDS_CONNECTORS_ALL
  issuer: http://omejdn:4567
id_token:
  expiration: 360000
  signing_key: signing_key.pem
  algorithm: RS256
  issuer: http://omejdn:4567
user_background:
- yaml
```

Build the Omejdn DAPS image

```
docker build -t daps .
```

Run the Omejdn DAPS server

```
docker run -d --name omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys --network=broker-localhost_default daps
```

# DATASPACE CONNECTOR:
The Testbed will have two built-in Connectors. They will be referred to as ConnectorA and ConnectorB. They will have different configurations, so they will each have their own directory. These directories are going to be referred to as `DataspaceConnectorA` and `DataspaceConnectorB`.

It is recommended to follow the guide with one Connector at a time to avoid configuration issues.

Make sure you are in the right directory:
```
cd Testbed/DataspaceConnectorA/
```
or
```
cd Testbed/DataspaceConnectorB/
```

## Component Documentation
The official documentation will cover the introductions, deployment, documentation and communication guide of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/DataspaceConnector/tree/v6.2.0

## Continue here after reading the official documentation
Official configuration documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Configuration#configuration

The Dataspace Connector must be configured to work in this environemnt.

## Changes to the application.properties file
Use nano or your most favourite editor.
```
nano src/main/resources/application.properties
```
### Spring Tomcat

**ConnectorA** is deployed in port `8080`
```
## Spring Tomcat
server.port=8080
```
**ConnectorB** is deployed in port `8081`
```
## Spring Tomcat
server.port=8081
```

### DAPS
Edit the DAPS configuration on both **ConnectorA** and **Connector B**. This will make use of the locally installed DAPS.
```
## DAPS
## daps.url=https://daps.aisec.fraunhofer.de
## daps.token.url=https://daps.aisec.fraunhofer.de/token
## daps.key.url=https://daps.aisec.fraunhofer.de/.well-known/jwks.json
## daps.key.url.kid={'https://daps.aisec.fraunhofer.de/.well-known/jwks.json':'default'}
daps.url=http://omejdn:4567
daps.token.url=http://omejdn:4567/token
daps.key.url=http://omejdn:4567/.well-known/jwks.json
daps.key.url.kid={'http://omejdn:4567/.well-known/jwks.json':'default'}
```

### TLS
Create a certificate with a specific DNS to use TLS and establish `https` connection. As a Docker network is used in the Testbed, the container name is used as DNS.

```
openssl req -x509 -newkey rsa:4096 -sha256 -days 2650 -nodes -keyout {NAME.key} -out {NAME.crt} -subj "/C={COUNTRY}/ST={STATE}/L={LOCALITY}/O={ORGANIZATION}/CN={COMMON_NAME}" -addext "subjectAltName=DNS:localhost,DNS:{CONTAINER_NAME}"
```

It could look something like this (**ConnectorA**)

```
openssl req -x509 -newkey rsa:4096 -sha256 -days 2650 -nodes -keyout connectorA.key -out connectorA.crt -subj "/C=ES/ST=Bizkaia/L=Bilbao/O=SQS/CN=connectorA" -addext "subjectAltName=DNS:localhost,DNS:connectora"
```

It could look something like this (**ConnectorB**)

```
openssl req -x509 -newkey rsa:4096 -sha256 -days 2650 -nodes -keyout connectorB.key -out connectorB.crt -subj "/C=ES/ST=Bizkaia/L=Bilbao/O=SQS/CN=connectorB" -addext "subjectAltName=DNS:localhost,DNS:connectorb"
```

The Dataspace Connector expects the TLS certificate in `.p12` format. Here is the command required:

```
openssl pkcs12 -export -out {NAME.p12) -inkey {NAME.key} -in {NAME.crt} -passout pass:password
```

It could look something like this (**ConnectorA**)
```
openssl pkcs12 -export -out connectora.p12 -inkey connectora.key -in connectora.crt -passout pass:password
```
It could look something like this (**ConnectorB**)
```
openssl pkcs12 -export -out connectorb.p12 -inkey connectorb.key -in connectorb.crt -passout pass:password
```

The main line of interest is `server.ssl.key-store=classpath:conf/{TLS_FILENAME}.p12`, where `{TLS_FILENAME}` is to be replaced with the TLS cert that was created above:

It could look something like this (**ConnectorA**)
```
## TLS
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:conf/connectora.p12
server.ssl.key-store-password=password
server.ssl.key-alias=1
#security.require-ssl=true
```
It could look something like this (**ConnectorB**)
```
## TLS
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:conf/connectorb.p12
server.ssl.key-store-password=password
server.ssl.key-alias=1
#security.require-ssl=true
```

## Changes to the config.json file
Use nano or your most favourite editor
```
nano src/main/resources/conf/config.json
```
### Deployment Mode
Edit `connectorDeployMode` from `TEST_DEPLOYMENT` to `PRODUCTIVE_DEPLOYMENT` for the connector to request and validate incoming DATs

```
  "ids:connectorDeployMode" : {
    "@id" : "idsc:PRODUCTIVE_DEPLOYMENT"
```

### Dataspace Connector KeyStore
```
  "ids:keyStore" : {
    "@id" : "file:///conf/{CERT_FILENAME}.p12"
```
{CERT_FILENAME} will be a certificate from the local CA or external to this testbed, provided by Fraunhofer AISEC (Contact Gerd Brost)

Ensure {CERT_FILENAME} are different for **ConnectorA** and **ConnectorB**

**Note:** Local CA certs will be available. Users can use those, create new ones or bring their own FH cert to replace {CERT_FILENAME}

### Open the `conf` directory
```
src/main/resources/conf/
```

Ensure the {CERT_FILENAME}.p12 file used for `ids:keyStore` is placed in this directory for the `config.json` to access it

Modify the `truststore.p12` for the Connector to accept these new TLS certificates. Make sure the {TLS_FILENAME}.crt is in this directory and then
```
keytool -import -alias {NAME} -file {NAME.crt} -storetype PKCS12 -keystore {truststore.p12}
```

It could look something like this (**ConnectorA**)
```
keytool -import -alias connectorA -file connectorA.crt -storetype PKCS12 -keystore truststore.p12
```

It could look something like this (**ConnectorB**)
```
keytool -import -alias connectorB -file connectorB.crt -storetype PKCS12 -keystore truststore.p12
```
It could look something like this (**Omejdn DAPS**)
```
keytool -import -alias omejdn -file omejdn.crt -storetype PKCS12 -keystore truststore.p12
```

You will be asked the following in the terminal:
* `Enter keystore password: `, type `password`
* `Trust this certificate? [no]: `, type `yes`

It should return:
```
Certificate was added to keystore
```

Ensure both connectorA.crt and connectorB.crt are imported into the truststore.p12

### When using the DSC for clarity reasons modify the following lines
Put a meaningful description to your connector
```
"ids:connectorDescription" : {
    "@type" : "ids:BaseConnector",
    "@id" : "https://w3id.org/idsa/autogen/baseConnector/7b934432-a85e-41c5-9f65-669219dde4ea"
```
Put a meaningful URL that uniquely identifies your connector towards the IDS Metadata Broker.
```
"ids:accessURL" : {
        "@id" : "https://localhost:8080/api/ids/data"
```
It could look something like this (**ConnectorA**)
```
"ids:connectorDescription" : {
    "@type" : "ids:BaseConnector",
    "@id" : "https://connector_A"
```
```
"ids:accessURL" : {
        "@id" : "https://connectora:8080/api/ids/data"
```
It could look something like this (**ConnectorB**)
```
"ids:connectorDescription" : {
    "@type" : "ids:BaseConnector",
    "@id" : "https://connector_B"
```
```
"ids:accessURL" : {
        "@id" : "https://connectorb:8081/api/ids/data"
```

### Additional Changes

For the use of this testbed, the Dataspace Connector must be built via docker.

Official build documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Build#docker

The testbed is run in a docker network defined earlier in this document called `broker-localhost_default`.

Before running your images as a container, add `--network=testbed` to the `docker run` command

```
docker build -t <IMAGE_NAME:TAG> .
docker run --publish 8080:8080 --detach --name {CONTAINER_NAME} --network=broker-localhost_default <IMAGE_NAME:TAG>
```

It could look something like this (**ConnectorA**)

```
docker build -t dsca .
docker run --publish 8080:8080 --detach --name connectora --network=broker-localhost_default dsca
```

It could look something like this (**ConnectorB**)

```
docker build -t dscb .
docker run --publish 8081:8081 --detach --name connectorb --network=broker-localhost_default dscb
```

This might take a while when you run it for the first time, as docker has to download some dependencies, build and run some tests.

> DSC will not fly without a daps token now. Make sure the DAPS runs first.


# METADATA BROKER

## Component Documentation
The official documentation will cover the pre-requisites, installation and deployment of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

## Continue here after reading the official documentation

## Changes to the application.properties file
Use nano or your most favourite editor.
```
nano broker-core/src/main/resources/application.properties
```
### DAPS
This will make use of the locally installed DAPS.

```
# DAPS
# daps.url=https://daps.aisec.fraunhofer.de
daps.url=http://omejdn:4567
daps.validateIncoming=true
```

### Security-related
Add the local DAPS to the trusted hosts

```
# Securiy-related
...
jwks.trustedHosts=daps.aisec.fraunhofer.de,omejdn
ssl.certificatePath=/etc/cert/server.crt
```

## Changes to the component's keystore
Use nano or your most favourite editor.
```
nano broker-core/src/main/resources/application.properties
```

Add the certificate provided by the local CA, newly created by the local CA or provided by Fraunhofer AISEC. If it is NOT provided by the local CA, make sure it is correctly added to the local DAPS.

```
keytool -importkeystore -srckeystore {SRCKEYSTORE} -srcstoretype {STORETYPE} -srcstorepass {SRCSTOREPASS} -destkeystore {DESTKEYSTORE} -deststoretype {DESTSTORETYPE} -deststorepass {DESTSTOREPASS}
```
It could look something like this

```
keytool -importkeystore -srckeystore testidsa10.p12 -srcstoretype pkcs12 -srcstorepass password -destkeystore isstbroker-keystore.jks -deststoretype jks -deststorepass password
```

Expected outcome:
```
"Import command completed:  1 entries successfully imported, 0 entries failed or cancelled"
```

To check the content of the created keystore, use the following command:

```
keytool -v -list -keystore {KEYSTORE}
```

It could look something like this

```
keytool -v -list -keystore isstbroker-keystore.jks
```

## Adding the TLS certificates

Create the following directory path:

```
sudo mkdir /etc/idscert/localhost
```

Copy the following files into this new path
* `server.crt`
* `server.key`

## Changes in the `docker-compose` file

Use nano or your most favourite editor.
```
nano docker/composefiles/broker-localhost
```

Ensure the container names are consistent with other dependencies by adding `container_name:`.

If port 80 is already in use, the `reverseproxy` container will exit with code 1. Follow the steps in the next block to get around this:

```
services:
  broker-reverseproxy:
    ...
    container_name: broker-localhost_broker-reverseproxy_1
    ...
    ports:
    - "443:443" # IDS-HTTP API
    - "80:80" # Change to a port of your choosing if taken: "{PORT}:80"
```

Make sure to edit the following lines to have the Metadata Broker in productive mode:

```
services:
  ...
  broker-core:
    ...
    environment:
    ...
    DAPS_VALIDATE_INCOMING=true
    ...
    DAPS_URL=http://omejdn:4567/token  
```

## Usage

Go to the directory with the `docker-compose` file and pull the images required
```
cd docker/composefiles/broker-localhost
docker-compose pull
```

The pulled `core` image contains pre-loaded important information, such as the component's keystore and the Fraunhofer DAPS. This pulled `core` image will not have the configuration required for this testbed and it will be re-created locally.

Delete the pulled `core` image

```
docker rmi registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core
```

Go back to the main directory and build the project with `maven`:

```
cd ../../..
mvn clean package
```

This will create a `.jar` file in `broker-core/target` that will have to be copied into `docker/broker-core`.

```
cp broker-core/target/broker-core-4.2.8-SNAPSHOT.jar docker/broker-core
```

Once the file is copied, move to the `docker/broker-core` directory and build the `core` image locally. The `core` image name will be maintained from the previously pulled `core` image. This will avoid dependency issues later on.

```
cd docker/broker-core
docker build -t registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core .
```

Go to the compose file and build the Metadata Broker

```
cd ../../docker/composefiles/broker-localhost/
docker-compose up
```
