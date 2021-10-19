# Testbed User Guide

## Purpose
This user guide is meant to explain to each testbed user what they should do with the testbed in order to assess the compatibility of their own developed component.

## Steps for testing a connector
### 1. Download the testbed and install it
Follow the instructions in the [installation and configuration guide](./README.md) to install and configure the testbed as required.

### 2. Generate a certificate for your connector
Use the following command to generate a certificate for your connector:
TODO: add command
Ensure that your connector always utilizes this IDS certificate to prove their identity with respect to the other components.

### 3. Configure your connector 
TODO: Define required actions - Configure support for Root CA (cert), DAPS (URL), Broker (URL)

### 4. Register your connector at the DAPS
TODO: Define required actions and expected outcome

### 5. Query the Broker for available data in the testbed
TODO: Define required actions and expected outcome

### 6. Contact available base connector to ask for self-description
TODO: Define required actions and expected outcome

### 7. Contact available base connector and request data
TODO: Define required actions and expected outcome

### 8. Register your connector at the IDS MetaDataBroker with the following data set
TODO: Define required actions and expected outcome

### 9. Utilize available base connector to request data from your connector
TODO: Define required actions and expected outcome

## In addition: Execute Test suite
TODO: reference to the Connector test suite

