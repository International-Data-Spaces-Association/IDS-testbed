# IDSA Reference Testbed Installation (CA-DAPS-DSC-MDB)

## Index
- [Execution modes](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#execution-modes)
    - [Hardware Requirements](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#hardware-requirements)
- [Target View: Preconfigured testbed](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#target-view-preconfigured-testbed)
- [Target View: Manual testbed set up](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#target-view-manual-testbed-set-up)
    - [Certificate Authority](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#certificate-authority)
    - [DAPS](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#daps)
    - [Dataspace Connector](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#dataspace-connector)
    - [Metadata Broker](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#metadata-broker)
- [Stop and delete testbed set up](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/InstallationGuide.md#stop-and-delete-testbed-set-up)

The installation and configuration process is explained below for each of the components. To further support this document, the links to the official installation guides will be linked.

# Execution modes
You may either run the preconfigured testbed offered in this repo or follow the instructions for the manual setup below to set it up on your own and possibly adjust it to your needs.

## Hardware Requirements

In this section the minimum requirements of disk space required for operating the IDS-testbed are detailed.

The current minimum requirements for the IDS-Testbed are:
- 4 GB RAM (however 8GB RAM is recommended)
- 50 GB storage

It is recommended to use 64bit quad core processor to provide enough processing power for all docker containers.

Take into account that if more components are included at the IDS-testbed or a huge amount of data is uploaded it is possible to run out of disk free space. In this cases it is recommended to provide more free disk storage.

# Target View: Preconfigured testbed
Follow this section to automatically launch the Preconfigured set up of the Testbed.

### Setting up requirements

The software required for the successful deployment of the testbed is the following:
- OS: Ubuntu 20.04.1 LTS
- Docker: 20.10.7
- Docker-compose: 1.27.4

First, verify your ubuntu version

```
lsb_release -a
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

Install docker and docker-compose
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

```
docker-compose version
```

The output should look similar to

```
docker-compose version 1.27.4, build unknown
docker-py version: 4.3.1
CPython version: 3.8.10
OpenSSL version: OpenSSL 1.1.1f  31 Mar 2020
```

If your docker-compose version is not the required one execute the following commands.

```
sudo apt-get install curl
sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

Download the `IDS-testbed` to your local environment.
```
sudo apt install git
git clone https://github.com/International-Data-Spaces-Association/IDS-testbed.git
```
Move to the downloaded directory and execute the `docker-compose.yml` script.
```
cd IDS-testbed
docker-compose up
```
If you face problems with docker or user rights execute the following commands and log out and back so that your group membership is re-evaluated.
```
sudo groupadd docker
sudo usermod -aG docker $USER 
# Where $USER is obtained by executing in the terminal the command `whoami`
# Log out and log back in so that your group membership is re-evaluated.
```
Re-execute the `docker-compose.yml` script.

The process of downloading the images and launching the containers of the different components (DAPS, DSC and MDB) will begin.

The IDS-testbed will be correctly deployed. The components that are part of the IDS-testbed can be reached at the URLs mentioned below.

DAPS:
* can be reached at https://localhost:443
* needs to be preconfigured to know connector A, B and the Broker

Connectors:
* connector A
  * can be reached at https://localhost:8080
  * needs to be preconfigured with a self-description and offering a dataset ("hallo world")

* connector B
  * can be reached at https://localhost:8081
  * needs to be preconfigured with a self-description and offering a dataset ("goodbye world")

Broker:
* can be reached at https://localhost:444
* needs to be aware of connector A, connector B and store their self-descriptions

# Target View: Manual testbed set up

Follow this section to manually launch the Testbed.
### Setting up requirements

The software required for the successful deployment of the testbed is the following:
- OS: Ubuntu 20.04.1 LTS
- Docker: 20.10.7
- Docker-compose: 1.27.4
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
docker-compose version 1.27.4, build unknown
docker-py version: 4.3.1
CPython version: 3.8.10
OpenSSL version: OpenSSL 1.1.1f  31 Mar 2020
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
openjdk version "11.0.13" 2021-10-19
OpenJDK Runtime Environment (build 11.0.13+8-Ubuntu-0ubuntu1.20.04)
OpenJDK 64-Bit Server VM (build 11.0.13+8-Ubuntu-0ubuntu1.20.04, mixed mode, sharing)
```
> To avoid problems while building components you should set the `JAVA_HOME`environment variable on your system.
>
> HINT: you might want to check with ``jrunscript -e 'java.lang.System.out.println(java.lang.System.getProperty("java.home"));'``


To enable the build process of the Data Space connector we install [maven](https://maven.apache.org/)

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
Java version: 11.0.13, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.13.0.28-generic", arch: "amd64", family: "unix"
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
docker network create testbed_local
```

## Download the Testbed
```
sudo apt install git
git clone https://github.com/International-Data-Spaces-Association/IDS-testbed.git
```

# CERTIFICATE AUTHORITY
Move to right directory, and make the files executable:

```
cd IDS-testbed/CertificateAuthority/
```

The official documentation will cover the scope, dependencies and usage of the component.

Official documentation: [CertificateAuthority/README.md](./CertificateAuthority/README.md)

The preconfigured setup includes certificates for:
* a root CA called "ReferenceTestbedCA"
* a subCA called "ReferenceTestbedSubCA" and
* certificates for devices called "testbed1", ... , "testbed10"

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

The certificate chain (CA, SubCA, Certs) has been created and the user should be able to create as many certificates as they need for their environment.

# DAPS

The official documentation of the Omejdn DAPS is here: https://github.com/International-Data-Spaces-Association/omejdn-daps

## Adding the keys to the DAPS

Every client that wants to use the local Omejdn DAPS must place their `{CERTFILE}.cert` file in the `keys` directory.

The directory can be found in

```
DAPS/keys
```

Add the certificate provided by the local CA, newly created by the local CA or provided by Fraunhofer AISEC. Place the certificate at the folder `DAPS/keys/omejdn/` with name `omejdn.key` to avoid dependency issues later on.

## Adding the clients to the DAPS

**Note:** The user must execute the `register_connector.sh` file in order to add the client to the Omejdn DAPS. Once executed, the certificate will be included in the DAPS's list of clients.

To execute the script

```
cd DAPS
./register_connector.sh {CERT_FILENAME} 
```

It could look something like this
```
./register_connector.sh testbed1
```

The certificate will be added to the list of DAPS's clients. You can check it at the file `DAPS/config/clients.yml`

## Required changes in the configuration

Change the configuration file `.env` with your favorite editor, e.g. `nano`.

```
nano .env
```

**Note** The file could be hidden. Select the option `show hidden files` and it should be placed at IDS-testbed root directory.

Replace the following lines with the necessary configuration. It could look something like this

```
COMPOSE_PROJECT_NAME=testbed
OMEJDN_ENVIRONMENT="production"
OMEJDN_PROTOCOL="https"
OMEJDN_VERSION="1.6.0"
OMEJDN_DOMAIN="omejdn"
OMEJDN_PATH="/auth"

ADMIN_USERNAME="admin"
ADMIN_PASSWORD="password"

TLS_KEY="${PWD}/DAPS/keys/TLS/daps.key"
TLS_CERT="${PWD}/DAPS/keys/TLS/daps.cert"
```

Configure the `docker-compose.yml` file with your configuration. Then run the Omejdn DAPS server.

The `docker-compose.yml` could look something like this

```
services

  omejdn:
    image: nginx:1.21.6
    container_name: omejdn
    ports:
      - 80:80
      - 443:443      
    environment:
      - OMEJDN_DOMAIN=${OMEJDN_DOMAIN}
      - OMEJDN_PATH=${OMEJDN_PATH}
      - UI_PATH=${UI_PATH}
    volumes:
      - ./DAPS/nginx.conf:/etc/nginx/templates/default.conf.template
      - ./DAPS/keys/TLS/daps.cert:/etc/nginx/daps.cert
      - ./DAPS/keys/TLS/daps.key:/etc/nginx/daps.key
    networks:
      - local

  omejdn-server:
    image: ghcr.io/fraunhofer-aisec/omejdn-server:${OMEJDN_VERSION}
    container_name: omejdn-server
    environment:
      - OMEJDN_ISSUER=${OMEJDN_ISSUER}
      - OMEJDN_FRONT_URL=${OMEJDN_ISSUER}
      - OMEJDN_OPENID=true
      - OMEJDN_ENVIRONMENT=${OMEJDN_ENVIRONMENT}
      - OMEJDN_ACCEPT_AUDIENCE=idsc:IDS_CONNECTORS_ALL
      - OMEJDN_DEFAULT_AUDIENCE=idsc:IDS_CONNECTORS_ALL
      - OMEJDN_ADMIN=${ADMIN_USERNAME}:${ADMIN_PASSWORD}
    volumes:
      - ./DAPS/config:/opt/config
      - ./DAPS/keys:/opt/keys
    networks:
      - local
      
networks:
  local:
    driver: bridge
```

## Required DNS for the DAPS
Create a certificate with a specific DNS to use TLS and establish https connection. As a Docker network is used in the Testbed, the container name is used as DNS.

```
openssl req -x509 -newkey rsa:4096 -sha256 -days 2650 -nodes -keyout {NAME.key} -out {NAME.crt} -subj "/C={COUNTRY}/ST={STATE}/L={LOCALITY}/O={ORGANIZATION}/CN={COMMON_NAME}" -addext "subjectAltName=DNS:localhost,DNS:{CONTAINER_NAME}"
```

It could look something like this

```
openssl req -x509 -newkey rsa:4096 -sha256 -days 2650 -nodes -keyout omejdn.key -out omejdn.crt -subj "/C=ES/ST=Bizkaia/L=Bilbao/O=SQS/CN=omejdn" -addext "subjectAltName=DNS:localhost,DNS:omejdn"
```

Place these created certificates at the folder `DAPS/keys/TLS/` and name them as `daps.crt` and `daps.key` to match the above mentioned `docker-compose.yml` file configuration.

# DATASPACE CONNECTOR:
The testbed will have two built-in Connectors. They will be referred to as ConnectorA and ConnectorB. They will have different configurations, so they will each have their own directory. These directories are going to be referred to as `DataspaceConnectorA` and `DataspaceConnectorB`.

It is recommended to follow the guide with one Connector at a time to avoid configuration issues.

Make sure you are in the right directory:
```
cd IDS-testbed/DataspaceConnectorA/
```
or
```
cd IDS-testbed/DataspaceConnectorB/
```

## Component Documentation
The official documentation will cover the introductions, deployment, documentation and communication guide of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/DataspaceConnector/tree/v7.1.0

## Continue here after reading the official documentation
Official configuration documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Configuration#configuration

The Dataspace Connector must be configured to work in this environment.

## Changes to the application.properties file
The configuration necessary for the application properties is located at the `src/main/resources/application.properties` folder of the official DSC repository. 

For the IDS-testbed deployment it is configured at the `docker-compose.yml`. Here it is detailed the port, daps configuration and the server ssl keystore. 

```
    ports:
      - 8080:8080
    environment:
      - DAPS_URL=https://omejdn
      - DAPS_TOKEN_URL=https://omejdn/auth/token
      - DAPS_KEY_URL=https://omejdn/auth/jwks.json
      - DAPS_INCOMING_DAT_DEFAULT_WELLKNOWN=/jwks.json
      - SERVER_SSL_KEY-STORE=file:///config/connectorA.p12
```

The server `server.ssl.key-store=file:///config/{TLS_FILENAME}.p12`, where `{TLS_FILENAME}` is to be replaced with the TLS cert that is created in the following section.

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
openssl pkcs12 -export -out connectorA.p12 -inkey connectorA.key -in connectorA.crt -passout pass:password
```
It could look something like this (**ConnectorB**)
```
openssl pkcs12 -export -out connectorB.p12 -inkey connectorB.key -in connectorB.crt -passout pass:password
```

## Changes to the config.json file
Use nano or your most favourite editor
```
nano DataspaceConnectorA/conf/config.json
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
{CERT_FILENAME} will be a certificate from the local CA or external to this testbed, provided by Fraunhofer AISEC (Contact Gerd Brost).

Ensure {CERT_FILENAME} are different for **ConnectorA** and **ConnectorB**

**Note:** Local CA certs will be available. Users can use those, create new ones or bring their own FH cert to replace {CERT_FILENAME}.

### Open the `conf` directory
```
DataspaceConnectorA/conf/
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

For the use of this testbed, the Dataspace Connector must be built via docker-compose.

The testbed is run in a docker network defined earlier in this document called `testbed_local`.

Configure the `docker-compose.yml` file with your configuration. The `docker-compose.yml` could look something like this for the DataspaceConnectorA.

```
services

  connectora:
    image: ghcr.io/international-data-spaces-association/dataspace-connector:7.1.0
    container_name: connectora
    ports:
      - 8080:8080
    networks:
      - local
    volumes:
      - ./DataspaceConnectorA/conf/config.json:/config/config.json
      - ./DataspaceConnectorA/conf/testbed1.p12:/conf/testbed1.p12
      - ./DataspaceConnectorA/conf/connectorA.p12:/config/connectorA.p12
      - ./DataspaceConnectorA/conf/truststore.p12:/config/truststore.p12
    environment:
      - CONFIGURATION_PATH=/config/config.json
      - DAPS_URL=https://omejdn
      - DAPS_TOKEN_URL=https://omejdn/auth/token
      - DAPS_KEY_URL=https://omejdn/auth/jwks.json
      - DAPS_INCOMING_DAT_DEFAULT_WELLKNOWN=/jwks.json
      - SERVER_SSL_KEY-STORE=file:///config/connectorA.p12

networks:
  local:
    driver: bridge
```

> DSC will not fly without a DAPS token now. Make sure the DAPS runs first.


# METADATA BROKER

## Component Documentation
The official documentation will cover the pre-requisites, installation and deployment of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

## Continue here after reading the official documentation

Download the component from the official repository

```
cd IDS-testbed
git clone -b 5.0.0 https://github.com/International-Data-Spaces-Association/metadata-broker-open-core.git
```

Use the downloaded component to build the broker-core image.

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
daps.url=https://omejdn/auth/token
daps.validateIncoming=true
```

### Security-related
Add the local DAPS to the trusted hosts

```
# Security-related
...
jwks.trustedHosts=daps.aisec.fraunhofer.de,omejdn
ssl.certificatePath=/etc/cert/server.crt
ssl.javakeystore=/etc/cert/isstbroker-keystore.jks
```

## Changes to the component's keystore
At the folder `broker-core/src/main/resources/` add the certificate provided by the local CA, newly created by the local CA or provided by Fraunhofer AISEC. If it is NOT provided by the local CA, make sure it is correctly added to the local DAPS.

```
keytool -importkeystore -srckeystore {SRCKEYSTORE} -srcstoretype {STORETYPE} -srcstorepass {SRCSTOREPASS} -destkeystore {DESTKEYSTORE} -deststoretype {DESTSTORETYPE} -deststorepass {DESTSTOREPASS}
```
It could look something like this

```
keytool -importkeystore -srckeystore testbed3.p12 -srcstoretype pkcs12 -srcstorepass password -destkeystore isstbroker-keystore.jks -deststoretype jks -deststorepass password
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

## Build the broker-core image

Go to the main directory and build the project with `maven`:

```
cd metadata-broker-open-core
mvn clean package
```

This will create a `.jar` file in `broker-core/target` that will have to be copied into `docker/broker-core`.

```
cp broker-core/target/broker-core-5.0.0.jar docker/broker-core
```

Once the file is copied, move to the `docker/broker-core` directory and place there the TLS certificate that corresponds to the DAPS. For the IDS-testbed it is located at `DAPS/keys/TLS/daps.cert` and use the following command to change the certificate format to `daps.crt`

```
openssl x509 -inform PEM -in daps.cert -out daps.crt
```

Then build the `core` image locally using the following command.

```
cd docker/broker-core
docker build -t registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core:5.0.0 .
```

## Adding the TLS certificates

At the `IDS-testbed/MetadataBroker/` folder place the TLS certificates together with the keystore.
* `server.crt`
* `server.key`
* `isstbroker-keystore.jks`

## Usage

Take the content from the file `metadata-broker-open-core/docker/composefiles/broker-localhost/docker-compose.yml` and copy it at your docker-compose.yml file. Use nano or your most favourite editor.
```
nano docker-compose.yml
```

Use the TLS certificates and ensure the container names are consistent with other dependencies by adding `container_name:`.

If port 443 is already in use, the `reverseproxy` container will exit with code 1. Follow the steps in the next block to get around this:

```
services:
  broker-reverseproxy:
    image: registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/reverseproxy
    container_name: broker-reverseproxy
    volumes:
      - ./MetadataBroker/server.crt:/etc/cert/server.crt
      - ./MetadataBroker/server.key:/etc/cert/server.key
    ports:
      - "443:443" # Change to a port of your choosing if taken: "{PORT}:443"
      - "80:80" # Change to a port of your choosing if taken: "{PORT}:80"
    networks:
      - local

  broker-core:
    image: registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core:5.0.0
    container_name: broker-core
    volumes:
      - ./MetadataBroker/isstbroker-keystore.jks:/etc/cert/isstbroker-keystore.jks
    environment:
      - SPARQL_ENDPOINT=http://broker-fuseki:3030/connectorData
      - ELASTICSEARCH_HOSTNAME=broker-elasticsearch
      - SHACL_VALIDATION=true
      - DAPS_VALIDATE_INCOMING=true
      - COMPONENT_URI=https://localhost/
      - COMPONENT_CATALOGURI=https://localhost/connectors/
      - DAPS_URL=https://omejdn/auth/token
    expose:
      - "8080" 
    networks:
      - local

  broker-fuseki:
    image: registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/fuseki
    container_name: broker-fuseki
    volumes:
      - broker-fuseki:/fuseki
    expose:
      - "3030"
    networks:
      - local
      
volumes:
  broker-fuseki:

networks:
  local:
    driver: bridge
```

Go to the compose file and build the Metadata Broker

```
docker-compose up
```

## Stop and delete testbed set up

In this section it is detailed how to automatically stop and delete IDS-testbed set up.

Be aware that following this section will stop and delete all the containers launched by the IDS-testbed. It will also remove the `docker-compose.yml` file configuration regarding the images of the different components and the associated docker volumes and network.

Move to your `IDS-testbed` downloaded directory and execute the following command
```
docker-compose down --rmi all -v
```

This is the expected outcome
```
Stopping connectorb          ... done
Stopping omejdn              ... done
Stopping broker-reverseproxy ... done
Stopping broker-core         ... done
Stopping broker-fuseki       ... done
Stopping omejdn-server       ... done
Stopping omejdn-ui           ... done
Stopping connectora          ... done
Removing connectorb          ... done
Removing omejdn              ... done
Removing broker-reverseproxy ... done
Removing broker-core         ... done
Removing broker-fuseki       ... done
Removing omejdn-server       ... done
Removing omejdn-ui           ... done
Removing connectora          ... done
Removing network testbed_local
Removing volume testbed_broker-fuseki
Removing image nginx:1.21.6
Removing image ghcr.io/fraunhofer-aisec/omejdn-server:1.6.0
Removing image ghcr.io/fraunhofer-aisec/omejdn-ui:dev
Removing image ghcr.io/international-data-spaces-association/dataspace-connector:7.1.0
Removing image ghcr.io/international-data-spaces-association/dataspace-connector:7.1.0
WARNING: Image ghcr.io/international-data-spaces-association/dataspace-connector:7.1.0 not found.
Removing image registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/reverseproxy
Removing image idstestbed/broker-core:5.0.0
Removing image registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/fuseki
```

As seen above, the containers have been stopped and deleted. The network, volumes and images used by the `docker-compose.yml` file have also been deleted.

