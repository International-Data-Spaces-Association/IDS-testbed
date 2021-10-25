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
* Installation of the components
* Interconnectivity of the components

## Installation of the components

### CERTIFICATION AUTHORITY

Official documentation: https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/CA

1. Installation
- Download the .zip from this repo and unzip the file
2. Initialization
- sudo apt install python3-openssl
- ./pki.py init
3. Usage
- Please refer to the official documentation link above. It covers the creation of the CA, Sub CA and Device Certificate.

The CA provides {cert}.crt and {cert}.key. Keep in mind that other formats will be required for the different components.

### DATASPACE CONNECTOR:

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector

1.	Quick start
- Download the .zip from this repo and unzip the file (v5.1.2, v6 coming soon)
- cd DataspaceConnector
- mvn clean package
- cd target
- java -jar dataspaceconnector-{VERSION}.jar (Version = 5.1.2)

If everything is working correctly, the connector can be found in https://localhost:8080. The API can be accessed at https://localhost:8080/api/docs, which requires the following authentication:

Username: admin	/	Password: password

It is important to know that this setup is for test environments and requires some changes to operate in the IDSA ecosystem. These will be explained in steps 2 and 3 below.

2.	Deployment

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Configuration

In DataspaceConnector/src/main/resources/conf/config.json
- Make sure to update the Connector to PRODUCTIVE_DEPLOYMENT
- Make sure to update the Connector with your own cert in "ids:keyStore" 

> docker build -t dsc .

> docker run --publish 8080:8080 --detach --name dsccontainer dsc

### DAPS

Official documentation: https://github.com/International-Data-Spaces-Association/omejdn-daps

1. Installation
SQS step by step guide, extracted from https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/SQS_DAPS.md
The Omejdn DAPS server can be launched with docker as explained in the official documentation above.

2. Configuration
Required modifications:
- Add the public keys from the certificates that will be used in the components requesting DATs
- Add the client's information (DAPS user) in config/clients.yml
- Change the host and audience in config/omejdn.yml
- To use the script, place the private keys from the certificates in the scripts directory. Furthermore, ensure that "iss" and "sub" in the second portion of the script (scripts/create_test_token.rb) have the same values.

> curl localhost:4567/token --data "grant_type=client_credentials&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_assertion={INSERT_TOKEN_HERE}&scope=ids_connector security_level"

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


