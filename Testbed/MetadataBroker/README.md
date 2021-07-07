# Metadata Broker 

## Software version
Version used in the current testbed: 4.0.3

## Requirements for the testbed
* Java 11
* Maven 3.6.3

## Component file
The Metadata Broker zip file to be used in the testbed is available in this directory.  

### Knowledge acquired

/MetadataBroker/metadata-broker-open-core-master/docker/composefiles/broker-localhost/docker-compose.yml

* Line 10: Reverseproxy ports. If 80 is already in use, change this to a free port (default: 80:80)
