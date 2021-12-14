# Information concerning the Utilized Reference Implementations

| Component | Specification | Open Source Implementation | Contact Person |
| --- | --- | --- | --- |
| Connector | [Connector interaction specification in IDS-G](https://github.com/International-Data-Spaces-Association/IDS-G/tree/main/Communication) (will come soon) [Connector specification in IDS-G](https://github.com/International-Data-Spaces-Association/IDS-G/tree/main/Components/Connector) (tbd) | [Dataspace Connector](https://github.com/International-Data-Spaces-Association/DataspaceConnector) [Trusted Connector](https://github.com/industrial-data-space/trusted-connector/)| Dataspace Connector: tbd; Trusted Connector:
gerd.brost@aisec.fraunhofer.de, michael.lux@aisec.fraunhofer.de, jean-luc.reding@aisec.fraunhofer.de |
| DAPS | [DAPS specification](https://github.com/International-Data-Spaces-Association/IDS-G/tree/main/Components/IdentityProvider/DAPS) | [Omejdn DAPS in IDS-G] https://github.com/International-Data-Spaces-Association/omejdn-daps | martin.schanzenbach@aisec.fraunhofer.de |
| MetaData Broker | [MetaData Broker specification in IDS-G](https://github.com/International-Data-Spaces-Association/IDS-G/tree/main/Components/MetaDataBroker)
[Whitepaper](https://internationaldataspaces.org/wp-content/uploads/IDSA-White-Paper-Specification-IDS-Meta-Data-Broker.pdf) | [MetaData Broker Open Core](https://github.com/International-Data-Spaces-Association/metadata-broker-open-core) | contact@ids.fraunhofer.de |
| ParIS | [ParIS specification in IDS-G](https://github.com/International-Data-Spaces-Association/IDS-G/tree/main/Components/IdentityProvider/ParIS) | [ParIS Open Core](https://github.com/International-Data-Spaces-Association/ParIS-open-core) | -- |

# Connector

## Dataspace Connector

The Dataspace Connector is an implementation of an IDS connector component following the
[IDS Reference Architecture Model](https://www.internationaldataspaces.org/wp-content/uploads/2019/03/IDS-Reference-Architecture-Model-3.0.pdf).
It integrates the [IDS Information Model](https://github.com/International-Data-Spaces-Association/InformationModel)
and uses the [IDS Messaging Services](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services)
for IDS functionalities and message handling.
The core component in this repository provides a REST API for loading, updating, and deleting
resources with local or remote data enriched by its metadata. It supports IDS conform message
handling with other IDS connectors and components and implements usage control for selected IDS
usage policy patterns.

### Further interesting resources:
* [Deployment Examples for Docker](https://github.com/International-Data-Spaces-Association/IDS-Deployment-Examples/tree/main/dataspace-connector)

## Trusted Connector

The Trusted Connector is an implementation of an IDS connector component following the
[IDS Reference Architecture Model](https://www.internationaldataspaces.org/wp-content/uploads/2019/03/IDS-Reference-Architecture-Model-3.0.pdf).

## IDCSP2

IDSCP is utilized as a connector interaction protocol. It's specification is provided in the IDS-G (see references in overview table).

## Repository with open source implementation.
* in Rust: https://github.com/International-Data-Spaces-Association/idscp2-rust
* in Java: https://github.com/industrial-data-space/idscp2-java

### Some remarks
Currently, the IDSCP2 Implementation focuses on the Transport Layer Protocol (as defined in https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/connector-interaction/Communication/protocols/idscp2/TransportLayer) which is used for establishing a secure communication channel between a client and a server application.
This secure channel is only established if a DAT token was provided which can be validated by the recipient and if Remote Attestation (necessary for Trust and Trust+ profiles) is conducted successfully. The sending and validation of the DAT and RAT details depends on different drivers which are currently not open source yet. The desired drivers to be used should (at least for the time being) be provided by the connector operator and it should be possible to bring own drivers into the system to be evaluated there.

### Contact Persons
Leon Beckmann (leon.beckmann@aisec.fraunhofer.de)  
Oliver Braunsdorf (oliver.braunsdorf@aisec.fraunhofer.de)  
Monika Huber (monika.huber@aisec.fraunhofer.de)  
Michael Lux (michael.lux@aisec.fraunhofer.de)  
Gerd Brost (gerd.brost@aisec.fraunhofer.de)
