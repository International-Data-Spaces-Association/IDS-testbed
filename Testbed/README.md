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

The CA scipt provided with the IDS testbed requires python. Install it with

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

First let us set up the network with

```
docker network create testbed
```

### CERTIFICATION AUTHORITY

Official documentation: https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/CA

1. Installation

You can download the .zip from the Certification Authority file and unzip the file or clone the testbed repository.

```
git clone https://github.com/International-Data-Spaces-Association/IDS-testbed.git

```

Move to right directory, unzip the file and make it executable:

```
cd Testbed/CertificationAuthority/
unzip CertificationAuthority.zip
cd CertificationAuthority
chmod a+x *.py 
```

2. Initialization

If not already done, please install pyhton3-openssl with `sudo apt install python3-openssl`

Then init the script by calling the init command. This will only initialize the data folder.

```
 ./pki.py init
```

The data folder will contain three empty folders `ca` `cert` `subca`.

3. Usage

Please refer to the [official documentation](https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/CA). It covers the creation of the CA, Sub CA and Device Certificate.

**CA**

The CA provides {cert}.crt and {cert}.key. Keep in mind that other formats will be required for the different components. Those have to be created.

You start to setup your CA with some basic information. Replace the parameters with suitable parameters for your use.

```
./pki.py ca create --common-name "Testbed CA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"

```

In the data/ca folder you should find now the follwing files:

```
'Testbed CA.crt'  'Testbed CA.key'  'Testbed CA.serial'
```

**SubCA**

The CA provides {cert}.crt and {cert}.key. Keep in mind that other formats will be required for the different components. Those have to be created.

You start to setup your CA with some basic information. Replace the parameters with suitable parameters for your use.

```
./pki.py subca create --CA "Testbed CA" --common-name "Testbed SubCA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"
```


In the data/subca folder you should find now the follwing files:

```
'Testbed SubCA.crt'  'Testbed SubCA.key'  'Testbed SubCA.serial'
```

**Certificates**

The CA provides {cert}.crt and {cert}.key. Keep in mind that other formats will be required for the different components. Those have to be created.

You start to setup your CA with some basic information. Replace the parameters with suitable parameters for your use.

```
./pki.py cert create --subCA "Testbed SubCA" --common-name "TestbedCert1" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS" --client --server
```


In the data folder you should find now the follwing files:

```
TestbedCert1.crt  TestbedCert1.key
```

Now we have to convert the generated certificates in `data/cert` to use them later in the Connector and DAPS using openssl.

```
## navigate to the folder data/cert
cd data/cert
openssl pkcs12 -export -out TestbedCert1.p12 -inkey TestbedCert1.key -in TestbedCert1.crt -passout pass:password
openssl pkcs12 -in TestbedCert1.p12 -out TestbedCert1.cert -nokeys -nodes -passin pass:password

```

You should now have two additional files in data/cert

```
TestbedCert1.cert  TestbedCert1.crt  TestbedCert1.key  TestbedCert1.p12
```

In case you work on the testbed repository you may directly copy the certificates to the DataSpace Connector and the DAPS. Otherwise let's do this later on.

```
## optinal for now 
cp TestbedCert1.p12 ../../../../DataspaceConnector/DataspaceConnector/src/main/resources/conf
cp TestbedCert1.cert ../../../../OmejdnDAPS/OmejdnDAPS/keys

```

Now you should have successfully set up the certicate chain for the testbed including one connector. 


### DATASPACE CONNECTOR:

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector

#### 1.	Quick start

You may download the .zip from this repo and unzip the file (v5.1.2, v6 coming soon) or get it directly from the DataSpace connector repository

If you chose to use the .zip file from this repository direcly and you have cloned the repo, just navigate to the folder

```
cd IDS-testbed/Testbed/DataspaceConnector/  
```
or refer to the [DataSpace Connector Repository](https://github.com/International-Data-Spaces-Association/DataspaceConnector/). [Version 5.1.2 is tagged](https://github.com/International-Data-Spaces-Association/DataspaceConnector/releases/tag/v5.1.2)

This instruction is based on a cloned IDS-testbed repository.

> Don't forget to set `JAVA_HOME` according to your system!

The following steps are optional. They show how to build and execute the DataSpace Connector.

```
unzip DataspaceConnector.zip
cd DataspaceConnector
mvn clean package
```

This might take a while as the DataSpace Connector has to download some dependencies and runs some test. 

Afterwards you can start the DataSpace Connector with
```
cd target
java -jar dataspaceconnector-{VERSION}.jar (Version = 5.1.2)
```

The result should look like this:
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:17 min
[INFO] Finished at: 2021-11-15T22:40:47+01:00
[INFO] ------------------------------------------------------------------------
```

If everything is working correctly, the connector can be found in https://localhost:8080. The API can be accessed at https://localhost:8080/api/docs, which requires the following authentication:

Username: admin	/	Password: password

It is important to know that this setup is for test environments and requires some changes to operate in the IDSA ecosystem. These will be explained in teh follwoing steps below.

#### 2.	Deployment

Official documentation: https://international-data-spaces-association.github.io/DataspaceConnector/Deployment/Configuration

We have to edit the configuration of the DataSpace Connector according to our setup. Open the application.properties, e.g. with `nano`or your most favorite editor

``
nano src/main/resources/application.properties 
``

and edit the DAPS configuration. Replace `localhost` with `omejdn`

```
## DAPS
##daps.token.url=https://daps.aisec.fraunhofer.de
##daps.key.url=https://daps.aisec.fraunhofer.de/v2/.well-known/jwks.json
daps.token.url=http://omejdn:4567/token
daps.key.url=http://omejdn:4567/.well-known/jwks.json
```

Then change the config.json file. We have to set the connector to PRODUCTVE_DEPLOYMENT and add our generated certificate here. Do not forget to provide your certificate, if you did not in the step above. 

```
nano src/main/resources/conf/config.json
```
Change to

```
  "ids:connectorDeployMode" : {
    "@id" : "idsc:PRODUCTIVE_DEPLOYMENT"
```

and 

```
  "ids:keyStore" : {
    "@id" : "file:///conf/keystore-localhost.p12,TestbedCert.p12"
```

Then we are ready to build the connector to docker.

```
 docker build -t dsc .

```

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


