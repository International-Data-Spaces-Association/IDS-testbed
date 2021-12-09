# IDSA Reference Testbed Installation (CA-DAPS-DSC)

Download the .zip files in this repository. The installation and configuration process is explained below for each of the components. To further support this document, the links to the official installation guides will be linked.

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
* can be reached at https://localhost:8082
* needs to be aware of connector A, connector B and store their self-descriptions

## Manual installation
The installation of the testbed executed the following steps:
1 setting up requirements
2 Installation of the components
3 Interconnectivity of the components

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

## CERTIFICATION AUTHORITY
Move to right directory, and make the files executable:

```
cd Testbed/CertificationAuthority/
chmod a+x *.py 
```

The official documentation will cover the scope, dependencies and usage of the component.

Official documentation: https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/CA

## Continue here after the official documentation has been followed

The Certification Authority provides {CERT_FILENAME}.crt and {CERT_FILENAME}.key formats. Keep in mind that other formats will be required for the different components. Those have to be created.

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


### DAPS

The official documentation of the Omejdn DAPS is here: https://github.com/International-Data-Spaces-Association/omejdn-daps

#### 1. Installation
The SQS step by step guide, extracted from https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/SQS_DAPS.md
The Omejdn DAPS server can be launched with docker as explained in the official documentation above.

2. Configuration
Required modifications:
- Add the public keys from the certificates that will be used in the components requesting DATs
- Add the client's information (DAPS user) in config/clients.yml
- Change the host and audience in config/omejdn.yml
- To use the script, place the private keys from the certificates in the scripts directory. Furthermore, ensure that "iss" and "sub" in the second portion of the script (scripts/create_test_token.rb) have the same values.


If you use the OmejdnDAPS from the testbed repo you can follow the steps below. Otherwise you can git clone the [OmejdnDAPS from its repository](https://github.com/International-Data-Spaces-Association/omejdn-daps) .

Go to the OmejdnDAPS folder and unzip it with:

```
cd ../../OmejdnDAPS
unzip OmejdnDAPS.zip
cd OmejdnDAPS
```

Change the configuration with your favorite editor, e.g. `nano`.
```
nano config/clients.yml
```
update the information on the certificates
- change `testClient` to `87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:10:3C:88:0E:53:02`
- change `testClient` to `TestbedCert.cert`

and your config file shoult look like this

```
---
- client_id:87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:1>  name: omejdn admin ui
  allowed_scopes:
  - omejdn:api
  redirect_uri: http://localhost:4200
  attributes: []
  certfile: TestbedCert.cert
- client_id: testClient2
  name: omejdn admin ui
  allowed_scopes:
  - omejdn:api
  redirect_uri: http://localhost:4200
  attributes: []
  certfile: testClient2
```

Then edit the `config/omejdn.yml` and change the mode to `idsc:IDS_Connectors_ALL` from `http://localhost:4567,idsc` and `TestServer`

Then your omejdn.yml should look like this
```
---
host: idsc:IDS_CONNECTORS_ALL
openid: true
token:
  expiration: 3600
  signing_key: keys/signing_key.pem
  algorithm: RS256
  audience: IDSC:IDS_CONNECTORS_ALL
  issuer: http://localhost:4567
id_token:
  expiration: 360000
  signing_key: keys/signing_key.pem
  algorithm: RS256
  issuer: http://localhost:4567
user_backend:
- yaml

```

Now you can docker build your DAPS with

```
docker build -t daps .
```

and then launch the OmejdnDAPS with

```
    docker run -d --name omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys --network=testbed daps
```

Verify if it runs on `http://localhost:4567`

And with

> curl localhost:4567/token --data "grant_type=client_credentials&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_assertion={INSERT_TOKEN_HERE}&scope=ids_connector security_level"



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

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector

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
docker build -t dscA .
docker run --publish 8080:8080 --detach --name connectora --network=broker-localhost_default dscA
```

It could look something like this (**ConnectorB**)

```
docker build -t dscB .
docker run --publish 8081:8081 --detach --name connectorb --network=broker-localhost_default dscB
```

This might take a while when you run it for the first time, as docker has to download some dependencies, build and run some tests.

> DSC will not fly without a daps token now. Make sure the DAPS runs first.



## Interconnectivity of the components

The following steps will show how to deploy the following:

CA -> DSC -> DAPS 
 
#### Testbed setup
Download all the components from the .zip files in this repository. The current versions DSC (v5.1.2) and DAPS (v0.0.2).

Unzip all the components and go into the Certification Authority folder in the terminal. 

#### Create a docker network
This will ensure that the components will be available to each other.

> docker network create testbed

This creates a docker network called "testbed"

#### Certification Authority

**Note:** The following example was performed by the SQS lab for the user's convenience and the certificates have already had their aki/ski extensions extracted. This allows the user to boot up a working Testbed. 

Inside the component´s folder, enter the following commands in the terminal one by one:

> chmod +x pki.py

> ./pki.py init

> ./pki.py ca create --common-name "Testbed CA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"

> ./pki.py subca create --CA "Testbed CA" --common-name "Testbed SubCA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"

> ./pki.py cert create --subCA "Testbed SubCA" --common-name "TestbedCert" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS" --client --server

> cd data/cert

> openssl pkcs12 -export -out TestbedCert.p12 -inkey TestbedCert.key -in TestbedCert.crt -passout pass:password

> openssl pkcs12 -in TestbedCert.p12 -out TestbedCert.cert -nokeys -nodes -passin pass:password

The command lines above create the CA, the Sub CA and the Device Certificate. Then, we go into the Device Certificate folder and use openssl to get .p12 and .cert formats. The .p12 is used for the Dataspace Connector and the .cert and .key are used in the Omejdn DAPS.

The cert directory in the CA now has the following:
- TestbedCert.crt (by default)
- TestbedCert.key (by default)
- TestbedCert.p12 (from the openssl command)
- TestbedCert.cert (from the openssl command)

#### Dataspace Connector

When the Dataspace Connector is unzipped, go into DataspaceConnector/DataspaceConnector/src/main/resources/conf and drop the {cert.p12} here. In this case, TesbedCert.p12.

Go into DataspaceConnector/DataspaceConnector/src/main/resources/conf/config.json. Two changes here:
 1. Change the connector deploy mode to PRODUCTIVE_DEPLOYMENT
 2. Change the connector keyStore to the new cert, TestbedCert.p12

Go into DataspaceConnector/DataspaceConnector/src/main/resources/application.properties. Change the following parameters to the DAPS involved in the Testbed. In this case, Omejdn.
 1. daps.token.url=http://omejdn:4567/token
 2. daps.key.url=http://omejdn:4567/.well-known/jwks.json

Launch the Dataspace Connector:

Use Docker:
> docker build -t dsc .

> docker run --publish 8080:8080 --detach --name dsccontainer --network=testbed dsc

The parameters above:
- publish: open port 8080
- detach: do not have the component running in the terminal
- name: name of the container
- network: docker network created for the different components in the Testbed

Check the Connector's self-description can be found at https://localhost:8080 and the API accessed at https://localhost:8080/api/docs (Authorization: username/password)

#### DAPS

When the Dataspace Connector is unzipped, go into OmejdnDAPS/OmejdnDAPS/keys and drop the {cert.cert} here. In this case, TesbedCert.cert.

Launch the Omejdn DAPS:

Use Docker:
> docker build -t daps .

> docker run -d --name=omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys --network=testbed daps

The parameters above:
- d (detach): do not have the component running in the terminal
- name: name of the container
- p (publish): open port 4567
- network: docker network created for the different components in the Testbed

Ensure the DAPS server is running: http://localhost:4567

A detailed interoperability guide has been developed by SQS in https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/SQS_DAPS.md. This guide makes use of the {Testbed} certificate. 

The guide above ensures interoperability with the help of the script within the DAPS. By giving the TestbedCert certificate to the Dataspace Connector, we ensure the interoperability between the two components. 

Interoperability can also be achieved without the use of the script. Point the Dataspace Connector to the Testbed DAPS as shown earlier. Go into the Dataspace Connector API (https://localhost:8080/api/docs) and perform:

> IDS Messages
> 
> POST 
> 
> /api/ids/connector/update (recipient url: https://brokerurl.es)

Check the obtained Dynamic Attribute Token by looking in the terminal:

> docker logs dsccontainer


