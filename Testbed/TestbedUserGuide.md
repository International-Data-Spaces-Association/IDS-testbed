# Testbed User Guide

## Purpose
This user guide is meant to explain to each testbed user what they should do with the testbed in order to assess the compatibility of their own developed component.

## Steps for testing your connector

### 1. Initial preparation
#### 1.1. Download the testbed and install it
Follow the instructions in the [installation and configuration guide](./README.md) to install and configure the testbed as required.
The easiest way for the checklist approach would be to use the preconfigured setup described or get in tough with someone who already has such a testbed up and running.

### 2. Integrating your connector into the ecosystem
#### 2.1. Generate a certificate for your connector
Generate a "Certificate Signing Request" CSR for the public key of your connector - TODO: check whether that is possible
Sign it with the private key of the CA available in TODO - TODO: add command
Ensure that your connector always utilizes this IDS certificate to prove their identity with respect to the other components.

#### 2.2. Configure your connector
* Configure the usage of the Root CA (cert) to be found in TODO
* Configure your connector to use the DAPS available under http://localhost:4567 (endpoints: /token, /.well-known/jwks.json)
* Provide a self-description for your connector

### 3. Interacting with the DAPS
#### 3.1. Register your connector at the DAPS
Register your connector following
a) the instructions provided here: 
https://github.com/International-Data-Spaces-Association/omejdn-daps#registering-connectors
TODO: recheck whether this is working as expected
or b) the manual steps described below:
TODO: explain what to do

#### 3.2. Request your DAT
* Use your connector to request a DAT from the DAPS
* Validate that you received a valid DAT corresponding to the specification: 
https://github.com/International-Data-Spaces-Association/IDS-G/blob/main/Components/IdentityProvider/DAPS/README.md#dynamic-attribute-token-dat

### 4. Interacting with connectors
#### 4.1. Request self-descriptions from available connectors
Connector A is available at the following URL: https://localhost:8080
Connector B is available at the following URL: https://localhost:8081

* Request the Self-Description from Connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - TODO: verify?
  * IDS-REST: not yet supported by connector A - TODO: verify?
* Validate that you receive the following self-description: [TODO: add link to the self-description]
* Request the Self-Description from Connector B using those of the following protocols that you support::
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - TODO: verify?
  * IDS-REST: not yet supported by connector A - TODO: verify?
* Validate that you receive the following self-description: [TODO: add link to the self-description]

#### 4.2. Request data sets from available connectors
Connector A offers data set "Hello world" at the following URL: ... 
Connector B offers data set "Goodbye world" at the following URL: ...  

* Request the "hello world" data sets from connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - TODO: verify?
  * IDS-REST: not yet supported by connector A - TODO: verify?
* Validate that you receive the following data set: [TODO: add link to the hello world dataset]
* Request the Self-Description from Connector B using those of the following protocols that you support::
  * Multipart: currently supported by connector B
  * IDSCP2: currently supported by connector B - TODO: verify?
  * IDS-REST: not yet supported by connector B - TODO: verify?
* Validate that you receive the following data set: [TODO: add link to the goodbye world dataset]

### 5. Interacting with the Broker
#### 5.1. Query the Broker for available data in the testbed
The Metadata Broker can be reached at https://localhost:8082 and is aware of the self-descriptions of connector A and B.

* Query the Metadata Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the Metadata Broker
  * IDSCP2: not yet supported by the Metadata Broker
  * IDS-REST: not yet supported by the Metadata Broker
* Validate that you received the following response:
[TODO: add the expected output]

#### 5.2. Register your connector at the IDS MetaDataBroker with the following data set
* Register your connector at the Metadata Broker using those of the following protocols you support:
  * Multipart: currently supported by the Metadata Broker
  * IDSCP2: not yet supported by the Metadata Broker
  * IDS-REST: not yet supported by the Metadata Broker
* Query the Metadata Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the Metadata Broker
  * IDSCP2: not yet supported by the Metadata Broker
  * IDS-REST: not yet supported by the Metadata Broker
* Validate that you received the following response:
[TODO: add the expected output + your own entered information]

### In addition: Execute Test suite
TODO: reference to the Connector test suite
