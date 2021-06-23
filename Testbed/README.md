# IDSA Reference Testbed Installation (rough draft)

Download the .zip files attached to this document. The installation process is explained below for each of the components. In case of requiring further explanation, the links to the original github repos will be added. The software required for the successful deployment of the testbed will also be mentioned.
Software and versions used for the testbed:
	Docker: 19.03.8
	Docker-compose: 1.25
	Java: 11.0.11
	Maven: 3.6.3
	Ruby: 2.7.0
	Curl: 7.68

## The installation of the testbed will be structured as follows:
* Installation of the components
* Interconnectivity of the components

## Installation of the components
### BROKER
1.	Prepare the SSL certificate: 
On your host system, create a directory /etc/idscert/localhost and put two files into this directory:
-	server.crt
-	server.key
These Keys should be obtained through Fraunhofer AISEC by contacting Gerd Brost (gerd.brost@aisec.fraunhofer.de)
 
2.	Build the Docker Images, Prepare and Check the Docker Compose File. 
We will make use of the docker images provided by the developers.
-	cd metadata-broker-open-core-master/docker/composefiles/broker-localhost
-	docker-compose pull
If reverseproxy gives an error with the proxy, listen tcp 0.0.0.0:80 bind: address already in use head to the docker-compose.yml in /metadata-broker-open-core/docker/composefiles/broker-localhost/docker-compose.yml line 10 and replace the first port. Example, “81:80”
3.	Run the services.
-	Docker-compose up

If everything is setup correctly, you can find the Broker’s self-description with https://localhost
An in-depth installation guide can be found in: https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

### DATASPACE CONNECTOR:
1.	Quick start
-	cd DataspaceConnector
-	mvn clean package
-	cd target
-	java -jar dataspaceconnector-5.1.1.jar
If everything is working correctly, find the self-description with https://localhost:8080 (this port number can be changed in DataspaceConnector/src/main/resources/application.properties line 6) and the API interface with https://localhost:8080/api/docs.
Username: admin	/	Password: password
It is important to know that this setup is for test environments and requires some changes to operate in the IDSA ecosystem. These will be explained in steps 2 and 3 below.
2.	Deployment
In DataspaceConnector/src/main/resources/conf/config.json
-	Line 12, replace
“@id” : “idsc:TEST_DEPLOYMENT” with “@id” : “idsc:PRODUCTIVE_DEPLOYMENT”
-	Line 16, replace
“@id” : https://w3id.org/idsa/autogen/baseConnector/7b934432-a85e-41c5-9f65-669219dde4ea with “@id” :  “CONNECTOR_URL” (your connector URL)
-	Line 60, replace
“@id” : “file:///conf/keystore-localhost.12” with “@id” : “file:///conf/{yourKeyStore.p12}
Save {yourKeyStore.p12} in /src/main/resources/conf.
3.	Repeat steps in the Quick Start with your newly updated configuration
-	cd DataspaceConnector
-	mvn clean package
-	cd target
-	java -jar dataspaceconnector-5.1.1.jar
If everything is working correctly, find the self-description with https://localhost:8080 (this port number can be changed in DataspaceConnector/src/main/resources/application.properties line 6) and the API interface with https://localhost:8080/api/docs.
Username: admin	/	Password: password
If everything has been successful with steps 2 and 3, the connector is ready to connect to the IDS ecosystem. Go to https://localhost:8080/api/docs with the login provided. 
Find the “IDS Messages” group. Click on the second entry:
POST /api/ids/connector/update – This call can be used for registering or updating the connector at the Fraunhofer broker.
 
Click on “Try it out” and for the recipient use: https://broker.ids.isst.fraunhofer.de/infrastructure
If everything works well, the server code is 200 and the response body shows 
“@type” : “ids:MessageProcessedNotificationMessage"
 

An in-depth installation guide can be found in https://international-data-spaces-association.github.io/DataspaceConnector/


### DAPS
1.	Running an Omejdn server
-	cd omejdn-daps
-	check you have the config/ and keys/ directories
-	docker run -d –name=omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys <dockerimage>
<dockerimage> can be built with the command: docker build . -t <imageName>
If it runs successfully, you can run the server in http://localhost:4567
2.	Adding a client
-	Place the public key (filename.cert) in the keys/ folder
-	Add a client in config/clients.yml
-	Change the client_id to your certification’s “filename”
-	Add the following line under attributes: 
certfile: filename.cert (the public key you placed in keys/)

 
3.	Request a token
-	Head to /scripts and place the clients private key (filename.key) in there
-	Open create_test_token.rb
-	Input the client in CLIENTID = “client_id user in clients.yml” 
-	Replace ‘keys/#{CLIENTID}’ with ‘#{CLIENTID}’
-	Run “ruby create_test_token.rb”, the token received can be further inspected in jwt.io
If you get a “cannot load such file – jwt”, fix it with “sudo gem install jwt”
-	Request a DAT with the token received in the previous step. Insert the token in {INSERT-THE-TOKEN-HERE} and run:
curl localhost:4567/token --data "grant_type=client_credentials&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_assertion={INSERT-THE-TOKEN-HERE}&scope=ids_connector security_level"
If everything was setup correctly, we will get a response with a DAT with specific expiration.

The installation guide can be found in: https://github.com/International-Data-Spaces-Association/omejdn-daps


### Interconnectivity of the components
Testbed connectivity
Connector + Broker
Build the Broker. Once it is setup make sure the self-description is showed in https://localhost.
Build the Dataspace Connector. Once it is setup make sure the self-description is showed in https://localhost:8080 (or the port you have specified in DataspaConnector/src/main/resources/application.properties line 6)
Once both are setup with their valid certificates as specified earlier in the document, open the Dataspace Connector API. 
 
Enter the address shown in the image https://localhost/infrastructure in the same API call that was performed while installing the Dataspace Connector. The broker can be found https://localhost and the /infrastructure endpoint is used to register and /or update the connector to the Broker.

Connector + Broker + DAPS
At the moment, this cannot be interconnected. There is an issue with the IDS Framework that was found during this step. This activity will remain on hold until the feature is updated.




