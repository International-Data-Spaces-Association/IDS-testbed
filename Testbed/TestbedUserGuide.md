# Testbed User Guide

## Purpose
This user guide is meant to explain to each testbed user what they should do with the testbed in order to assess the compatibility of their own developed component.

## Steps for testing a connector
### 1. Initial preparation
#### 1.1. Download the testbed and install it
Follow the instructions in the [installation and configuration guide](./README.md) to install and configure the testbed as required.
TODO: or get in contact with someone offering a running instance 

### 2. Integrating your connector into the ecosystem
#### 2.1. Generate a certificate for your connector
Use the following command to generate a certificate for your connector:
TODO: add command
Ensure that your connector always utilizes this IDS certificate to prove their identity with respect to the other components.

#### 2.2. Configure your connector 
TODO: Define required actions - Configure support for Root CA (cert)

### 3. Interacting with the DAPS
#### 3.1. Register your connector at the DAPS
TODO: Define required actions and expected outcome

#### 3.2. Request your DAT
TODO: Define required actions and expected outcome

### 4. Interacting with connectors
#### 4.1. Request self-descriptions from available connectors
Connector A is available at the following URL: ...
Connector B is available at the following URL: ...
Request the Self-Description from both of them
##### 4.1.1 Multipart
##### 4.1.2 IDS-REST
to be added later on?
##### 4.1.3 IDSCP2

#### 4.2. Request data sets from available connectors
Connector A offers data set "Hello world" at the following URL: ...
Connector B offers data set "Goodbye world" at the following URL: ...
Request those data sets from both of them
##### 4.2.1 Multipart
##### 4.2.2 IDS-REST
to be added later on?
##### 4.2.3 IDSCP2

### 5. Interacting with the Broker
#### 5.1. Query the Broker for available data in the testbed
TODO: Define required actions and expected outcome
##### 5.1.1 Multipart
##### 5.1.2 IDS-REST
to be added later on?
##### 5.1.3 IDSCP2 
to be added later on

#### 5.2. Register your connector at the IDS MetaDataBroker with the following data set
TODO: Define required actions and expected outcome
##### 5.2.1 Multipart
##### 5.2.2 IDS-REST
to be added later on?
##### 5.2.3 IDSCP2 
to be added later on

### In addition: Execute Test suite
TODO: reference to the Connector test suite
