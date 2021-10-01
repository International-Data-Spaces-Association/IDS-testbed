# IDSA Reference Testbed Installation (WORK IN PROGRESS)

Download the .zip files in this repository. The installation and configuration process is explained below for each of the components. To further support this document, the links to the official installation guides will be linked.

The software required for the successful deployment of the testbed will also be mentioned.
Software and versions used for the testbed:
- OS: Ubuntu 20.04.1 LTS
- Docker: 19.03.8
- Docker-compose: 1.25
- Java: 11
- Maven: 3.6.3
- Ruby: 2.7.0

## The installation of the testbed will be structured as follows:
* Installation of the components
* Interconnectivity of the components (upcoming)

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

### DATASPACE CONNECTOR:

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector

1.	Quick start
- Download the .zip from this repo and unzip the file (v5.1.2, v6 coming soon)
- cd DataspaceConnector
- mvn clean package
- cd target
- java -jar dataspaceconnector-{VERSION}.jar (Version = 5.1.2 at the moment)

If everything is working correctly, the connector can be found in https://localhost:8080. The API can be accessed at https://localhost:8080/api/docs, which requires the following authentication:

Username: admin	/	Password: password

It is important to know that this setup is for test environments and requires some changes to operate in the IDSA ecosystem. These will be explained in steps 2 and 3 below.

2.	Deployment

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Configuration

In DataspaceConnector/src/main/resources/conf/config.json
- Make sure to update the Connector to PRODUCTIVE_DEPLOYMENT
- Make sure to update the Connector with your own cert in "ids:keyStore" 

3.	Repeat steps in the Quick Start with your newly updated configuration
-	cd DataspaceConnector
-	mvn clean package
-	cd target
-	java -jar dataspaceconnector-5.1.2.jar


### DAPS

Official documentation: https://github.com/International-Data-Spaces-Association/omejdn-daps

SQS step by step guide, extracted from https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/SQS_DAPS.md

The Omejdn DAPS server can be launched with docker as explained in the official documentation above.

Required modifications:
- Add the public keys from the certificates that will be used in the components requesting DATs
- Add the clients (DAPS users) in clients.yml
- Change the host and audience in omejdn.yml
- To use the script, place the private keys from the certificates in the scripts directory. Furthermore, ensure that "iss" and "sub" in the second portion of the script (create_test_token.rb) have the same values.

> curl localhost:4567/token --data "grant_type=client_credentials&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_assertion={INSERT_TOKEN_HERE}&scope=ids_connector security_level"

## Interconnectivity of the components (upcoming)

The following steps will show how to deploy the following:

CA -> DSC -> DAPS 
 
#### Testbed setup
Download all the components from the .zip files in this repository. The current versions DSC (v5.1.2) and DAPS (v0.0.2).

Unzip all the components and go into the Certification Authority folder in the terminal. 

#### Certification Authority
Go into the component folder and enter the following commands in the terminal one by one:
> chmod +x pki.py

> ./pki.py init

> ./pki.py ca create --common-name "Testbed CA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"

> ./pki.py subca create --CA "Testbed CA" --common-name "Testbed SubCA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"

> ./pki.py cert create --subCA "Testbed SubCA" --common-name "TestbedCert1" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS" --client --server
