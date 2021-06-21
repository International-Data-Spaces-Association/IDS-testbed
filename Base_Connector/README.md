# Dataspace Connector (as Base Connector)

The Dataspace Connector is an implementation of an IDS connector component following the
[IDS Reference Architecture Model](https://www.internationaldataspaces.org/wp-content/uploads/2019/03/IDS-Reference-Architecture-Model-3.0.pdf).
It integrates the [IDS Information Model](https://github.com/International-Data-Spaces-Association/InformationModel)
and uses the [IDS Connector Framework](https://github.com/FraunhoferISST/IDS-Connector-Framework)
for IDS functionalities and message handling.
The core component in this repository provides a REST API for loading, updating, and deleting
resources with local or remote data enriched by its metadata. It supports IDS conform message
handling with other IDS connectors and components and implements usage control for selected IDS
usage policy patterns.

## Link to the current specification
* [IDS specification for Connector communication (draft)](https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/connector-interaction/Communication)
* [Dataspace Connector Documentation](https://international-data-spaces-association.github.io/DataspaceConnector/)


## Repository with open source implementation
* [Dataspace Connector Source Code](https://github.com/International-Data-Spaces-Association/DataspaceConnector)
* [Dataspace Connector Core latest docker file](https://github.com/International-Data-Spaces-Association/DataspaceConnector/pkgs/container/dataspace-connector)
* [Deployment Examples for Docker](https://github.com/International-Data-Spaces-Association/IDS-Deployment-Examples/tree/main/dataspace-connector)

## Installation Guide
* [Quick start (without valid Identity in IDS)](https://international-data-spaces-association.github.io/DataspaceConnector/GettingStarted)
* [Installation Guide](https://international-data-spaces-association.github.io/DataspaceConnector/Deployment)
* [Deployment Examples for Docker](https://github.com/International-Data-Spaces-Association/IDS-Deployment-Examples/tree/main/dataspace-connector)

## Other interesting Documents
### User API
* [Dataspace Connector Communication Guide](https://international-data-spaces-association.github.io/DataspaceConnector/CommunicationGuide)
* [Dataspace Connector REST API](https://international-data-spaces-association.github.io/DataspaceConnector/Documentation/RestApi)

### IDS API
* [Dataspace Connector Messages](https://international-data-spaces-association.github.io/DataspaceConnector/Documentation/Messages)
* [IDS Message Flow](https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/connector-interaction/Communication/sequence-diagrams/data-connector-to-data-connector#message-flows-for-connector-to-connector-communication)

---

**Note:** The interface via which an IDS connector expects IDS messages is individual for each connector. This can be viewed via the self-description, which can be requested for the Dataspace Connector via ```GET {connector-url}/```. The interface for message processing is specified for the DSC with ```{connector-url}/api/ids/data```.

---
