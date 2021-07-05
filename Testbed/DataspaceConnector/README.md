# Dataspace Connector

## Software version
Version used in the current testbed: 5.1.2

## Requirements for the testbed
* Java 11
* Maven 3.6.3
## Component file
The Dataspace Connector zip file to be used in the testbed is available in this directory.

### Knowledge acquired
/DataspaceConnector/src/main/resources/application.properties
* Line 6: Port configuration for the UI (default: 8080)
* Line 25: API username (default: admin)
* Line 26: API password (default: password)
* Line 29: Endpoint for the api (default: /api/docs)
* Line 52: keyStorePassword (default: password)
* Line 54: trustStorePassword (default: password)
* Line 56: DAPS. Lines 57 and 58 will be edited to connect the DSC to the testbed DAPS
* Line 62: Clearing House. Line 63 will be edited to connect the DSC to the testbed Clearing House (future)
