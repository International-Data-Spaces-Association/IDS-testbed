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

1. Install the reference testbed following the [installation and configuration guide](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/README.md) in the Testbed Folder . You can either use the Preconfigured set up or install every component manually. Current available components are: Dataspace connector, DAPS, CA, Metadata Broker.

2. Connect the components to one another following the instructions of the "Interconnectivity of the components", the last section of the [installation and configuration guide](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/README.md)

3. Test the compatibility of your own developed component following the steps of the [Testbed User Guide.](https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/TestbedUserGuide.md)

4. Download the [Test Suite](https://gitlab.cc-asp.fraunhofer.de/ksa/ids-certification-testing) and follow the instructions to conduct automated tests for your own developed connector

## Current version

![image](https://user-images.githubusercontent.com/77682996/140496334-8e96dbdc-9785-45f9-9c28-ec433ca13dc5.png)


## [TODO](./TODO.md)

```
testbed -
         |
          - CA
         |
          - DAPS
         |
          - Clearing House          
         |
          - ParIS
         |
          - Meta Data Broker 'basic'
         |
          - Meta Data Broker 'advanced information'
         |
          - Meta Data Broker 'usage control'          
         |
          - Facility 'Base' Connector
         |
          - Facility 'Trusted' Connector 
         |
          - Facility 'Trusted Plus' Connector
         |
          - Applicant          
          
```

---
