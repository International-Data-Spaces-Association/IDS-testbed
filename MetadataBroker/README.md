# MetaData Broker

## Link to the current specification
Official  
* IDS-G: https://github.com/International-Data-Spaces-Association/IDS-G/tree/master/core/MetaDataBroker
* Whitepaper: https://internationaldataspaces.org/wp-content/uploads/IDSA-White-Paper-Specification-IDS-Meta-Data-Broker.pdf

Internal Preparation  
https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/IDS-Meta-Data-Broker-Spec/Components/MetaDataBroker

## Repository with open source implementation
https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

### Installation Guide
This document aims to aid IT administrators or developers in the installation of the IDS Metadata Broker.  
  
Note: this guide works with provided images and is not targeted for development purposes. Thus, instructions for building and editing the docker image file will not be included here.

#### 1 Recommended System Specifications

In this section, we will provide some guidance as to recommendations for the number of resources that should be available to smoothly operate the IDS Metadata Broker. The actual number of resources required heavily depends on the load. In case of very little traffic, fewer resources than listed below might be required.


##### 1.1 Hardware

2GB of disk space is required for operating the IDS Metadata Broker, though we recommend providing at least 20GB of free disk storage to avoid running out of disk space with increasing number of registered items. We also recommend reserving at least 2GB of RAM to ensure a responsive frontend.

To provide enough processing power for all Docker containers, we recommend using a 64bit quad core processor or better.

##### 1.2 Software

We recommend using a Linux based operating system. However, any operating system with a Docker installation can be used (tested on Ubuntu 20.04 and Windows 10). More strict hardware requirements than listed above might apply if a non-Linux operating system is used.

##### 1.3 Other

A valid X.509 certificate, signed by a trusted certification authority, is strongly recommended to avoid warnings about insecure HTTPS connections. Docker must be installed on the target machine.
For the SSL certificate you need to have these two files:
-   **server.crt:** an x509 certificate, either self-signed or from an official CA
-   **server.key:** the private key for the certificate
    
The certificate needs to be of .crt format and must have the name server.crt and the file for private key should have the name server.key. In case your certificate is of .pem format, it can be converted with the following commands, which require OpenSSL to be installed:

		openssl x509 -in mycert.pem -out server.crt
		openssl rsa -in mykey.pem -out server.key
		mkdir cert
		mv server.crt cert/
		mv server.key cert/


#### 2 Configuring the Docker-Compose File
The docker-compose file is located in path:

	<docker/composefiles/Meta-Data-Broker/broker-localhost/docker-compose.yml >

  


The most crucial part of adapting the configuration is to provide the correct location of the X.509 certificate in the broker-reverseproxy service. 

**For Linux users:**  if the location of the certificate is *“/home/ids/cert”*, the corresponding configuration in the yml file is:
		
	services: broker-reverseproxy:
		image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker/reverseproxy
		volumes:
		- /home/ids/cert:/etc/cert/
		[…]



**For Windows users:**  if the location of the certificate is *“c:/etc/ids/cert”*, the corresponding configuration in the yml file is:  


	services: broker-reverseproxy:
		image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker/reverseproxy
		volumes:
		- c:/etc/ids/cert:/etc/cert/
		[…]

#### 3 Downloading the Docker Images

All the IDS Metadata Broker Docker images are hosted at the GitLab of Fraunhofer IAIS. No credentials needed to download the images. The following command is for pulling all docker images:

		docker-compose pull

Note that this command should be executed in the same path of docker-compose.yml file.
  

#### 4 Starting up the IDS Metadata Broker

As a last preparation step, make sure that the max virtual memory areas are set to 262144 or higher, otherwise Elasticsearch will produce an error on startup. This can be achieved by either running the command:

		sysctl -w vm.max_map_count=262144

Alternatively, to ensure this modification is kept upon rebooting, the following line can be appended to /etc/sysctl.conf:

		vm.max_map_count=262144

To start up the IDS Metadata Broker, run the following command inside the directory of the docker-compose.yml file:

		docker-compose up –d

  

This process can take several minutes to complete. You can test whether the IDS Metadata Broker has successfully started by opening [https://localhost](https://localhost/). The result should be a JSON document, providing some general metadata about the IDS Metadata Broker. Appending “/query” to the address should result in the human user-friendly search interface being loaded instead.

Furthermore, the docker-compose logs command can be used to access the logs for a docker-compose.yml file, see [here](https://docs.docker.com/compose/reference/logs/).

  

#### 5 Interacting with the IDS Metadata Broker


The IDS Metadata Broker accepts and sends messages according to the IDS information model. This model uses the Resource Description Framework (RDF) to leverage the power of linked data. Many examples about representations of IDS concepts can be found at [https://github.com/International-Data-Spaces-Association/InformationModel/tree/develop/examples](https://github.com/International-Data-Spaces-Association/InformationModel/tree/develop/examples).

The multipart endpoint of IDS Metadata Broker is “/infrastructure”. If the IDS Metadata Broker is running using docker-compose as mentioned earlier, an HTTP POST request can be sent to interact with it. We provide some example messages, illustrating all core functions of the IDS Metadata Broker in this  [postman collection](https://www.getpostman.com/collections/1cecd0def2941a993e80).

In addition to the multipart endpoint, the IDS Metadata Broker also serves a prototypical [IDS-REST](https://www.getpostman.com/collections/01d6bf596f67303c08ce) endpoint at “/catalog”. This endpoint will reach a non-prototype state soon after the final specification of the IDS-REST protocol.
  

#### 6 Updating the IDS Metadata Broker

To update an existing installation of the IDS Metadata Broker, first repeat the steps explained in section “Downloading the Docker Containers”. Containers can be either hot updated or restarted to apply the changes. To hot update a container, run the following command:

		docker-compose up -d --no-deps --build <container name>

Alternatively, one can restart the entire service by running:

		docker-compose down
		docker-compose up –d


**Note that the Metadata Broker is still actively maintained by Fraunhofer IAIS. In case of errors, please send an email to contact@ids.fraunhofer.de.

### Other interesting Documents

Contact Information: matthias.boeckmann@iais.fraunhofer.de
