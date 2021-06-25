# Review - Meta Data Broker

[Link to Specifications](https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/MetadataBroker)

## Feedback 

1. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

*The goal of this implementation is to show how the concepts introduced in the Handshake Document can be turned into an actual application.*
If the handshake document is referred to, please provide and link to this document.
"IDS Information Model for core communication tasks"
If the document is referred to, please provide and link to this document.

2. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core

No installation instructions are available.
The expectation is that all necessary steps for the installation are given.

Good Example:
```bash
git clone https://github.com/International-Data-Spaces-Association/metadata-broker-open-core
cd metadata-broker-open-core
./install
```

3. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/LICENSE

The License file is given "Apache License 2.0", thats is fine.

4. Topic

Is there a list of further software components? 

5. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core 

*Running the Broker* -> There must be two separate instructions, one for Linux and one for Windows. No mixing, this only leads to confusion for the user.

6. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core 

*Running the Broker* -> These instructions are intended to map the commands for execution.

Don't write *create a directory /etc/idscert/localhost*
Better write:
```bash
mkdir /etc/idscert/
cd /etc/idscert/
touch server.crt
touch server.key
```

7. Topic

```bash
openssl 
cd tmp
openssl x509 -in example_cert.pem -out server.crt openssl rsa -in example_key.pem -out server.key 
```
There is no version specification for openssl.

8. Topic

```bash
openssl 
mkdir cert 
mv server.crt cert/ 
mv server.key cert/
```

At which path should the key be created?
Do you want to insert the key inside files server.crt and server.key created above?

9. Topic

*Build the Docker Images, Prepare and Check the Docker Compose File* There are no commands available. What should be done here?

10. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/docker/buildImages.sh

The script has dependencies that are not described. 
* mvn
* docker 

We need the version and installation instructions for it.

11. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/docker/buildImages.sh 
```bash
docker build broker-core/ -t registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core
```
The command poses a problem. In company networks, access to the specified port is not always possible.

12. Topic

https://app.swaggerhub.com/apis/idsa/IDS-Broker/1.3.1

That is very good.

13. Topic

We need a clarification how to install the software. Download from GIT and compile it yourself and / or download and start a Docker container. Which variant is the preferred?

14. Topic

There ist no SBOM (https://cyclonedx.org/use-cases/) available?

15. Topic

No installation instructions are available.

16. Topic

https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/broker-core/pom.xml
https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/broker-core/src/test/java/de/fraunhofer/iais/eis/ids/broker/handler/RegistrationHandlerTest.java
https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/broker-core/src/test/java/de/fraunhofer/iais/eis/ids/broker/persistence/RdfPersistenceTest.java

```
<!-- JSON. TODO, do testing in a better way, not requiring this dependency  -->
```
It is not recommended to write ToDo's in sourcecode.



