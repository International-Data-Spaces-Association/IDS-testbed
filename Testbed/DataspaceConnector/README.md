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
* Line 60: Clearing House. Line 61 will be edited to connect the DSC to the testbed Clearing House **(FUTURE)**
* Line 64: Policy.negotiation. Can be changed later in the UI (default: true)
* Line 65: Policy.allow-unsupported-patterns. Can be changed later in the UI (default: false)
* Line 103: Path to the keystore file (default: keystore-localhost.p12)
* Line 104: Keystore password (default: password)

/DataspaceConnector/src/main/resources/conf/config.json
* Line 12: Connector deploy mode. TEST_DEPLOYMENT vs PRODUCTIVE DEPLOYMENT (default: TEST_DEPLOYMENT)
* Line 34: Access URL (default: https://localhost:8080/api/ids/data)
* Line 60: keyStore. Configure your keyStore .p12 file here (default: keystore-localhost.p12)

https://localhost:8080/api/docs username:admin password:password

IDS Messages > /api/ids/connector/update: Allows the user to register to the Broker of their choosing
