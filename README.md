# IDS-testbed

## What is it?

Set up with Open Source IDS components which can be used to verify that a component:​
- implements the IDS specifications for establishing connections and communication.​
- and, thus, can work interoperable with all IDS components in the testbed setup.

## What is it for?

- Component behaviour testing
- Interoperability testing against IDS components (Connector, DAPS, CA, Metadata Broker)
- Preparation for IDS certification
- Starting point for creation of data spaces

## How to get started?

1. Git clone this repository to your local machine and go into the directory (usually ```cd IDS-testbed```) 

2. Make sure you have installed Docker and Docker Compose on your machine. Run ```docker compose up -d``` or ```docker-compose up -d``` to start the Testbed. 
   
3. Run the [Postman collection](./Testbed_Preconfiguration.postman_collection.json) to start the tests. Current available components for testing are: Dataspace connector, DAPS, CA, Metadata Broker.

4. Test the compatibility of your own developed component following the steps of the [Testbed User Guide](./TestbedUserGuide.md).

5. Download the [Test Suite](https://gitlab.cc-asp.fraunhofer.de/ksa/ids-certification-testing) and follow the instructions to conduct automated tests for your own developed connector

## Current version (V1.0)

Minimal setup with essential and already available components
![first_version](./pictures/Testbed_1.0.png)

## Roadmap
### Version 2.0 of the test bed

Minimal viable data space with all essential components and first test cases
![next_version](./pictures/Testbed_2.0.png)

### Version X.Y of the test bed

Integration of all intended components into the setup
![next_version](./pictures/Testbed_X.Y.png)

### Final vision for the testbed

On the long run, the testbed should be equipped with a test suite and testing components replacing the  open source reference implementations of the components.
![vision](./pictures/Testbed_vision.png)
