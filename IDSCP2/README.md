# IDCSP2

## Link to the current specification
Official

Internal Preparation
https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/connector-interaction/Communication/protocols/idscp2

## Repository with open source implementation
* in Rust: https://github.com/International-Data-Spaces-Association/idscp2-rust
* in Java: https://github.com/industrial-data-space/idscp2-java

### Some remarks
Currently, the IDSCP2 Implementation focuses on the Transport Layer Protocol (as defined in https://github.com/International-Data-Spaces-Association/IDS-G-pre/tree/connector-interaction/Communication/protocols/idscp2/TransportLayer) which is used for establishing a secure communication channel between a client and a server application.
This secure channel is only established if a DAT token was provided which can be validated by the recipient and if Remote Attestation (necessary for Trust and Trust+ profiles) is conducted successfully. The sending and validation of the DAT and RAT details depends on different drivers which are currently not open source yet. The desired drivers to be used should (at least for the time being) be provided by the connector operator and it should be possible to bring own drivers into the system to be evaluated there.

### Installation Guide
The IDSCP2 Core with the logic to use different drivers can be installed with the following instructions:

* in Rust: https://github.com/International-Data-Spaces-Association/idscp2-rust#building  
System Requirements: the IDSCP2 Rust implementation currently only runs under Linux (tested with Ubuntu 18.04 and 20.04)

* in Java: https://github.com/industrial-data-space/idscp2-java#build

### Usage Instructions
* For the Rust-Implementation:
https://github.com/International-Data-Spaces-Association/idscp2-rust#as-socket-tunnel

* For the Java Implementation: 
Maven artifacts are pushed to maven central and can be found here: https://search.maven.org/search?q=idscp2.  
More information about the usage can be found in the [IDSCP2 Documentation](https://github.com/industrial-data-space/idscp2-java/wiki).

### Other interesting Documents

### Contact Persons
Leon Beckmann (leon.beckmann@aisec.fraunhofer.de)  
Oliver Braunsdorf (oliver.braunsdorf@aisec.fraunhofer.de)  
Monika Huber (monika.huber@aisec.fraunhofer.de)  
Michael Lux (michael.lux@aisec.fraunhofer.de)  
Gerd Brost (gerd.brost@aisec.fraunhofer.de)  
