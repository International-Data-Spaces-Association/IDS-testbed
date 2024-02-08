Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- none 

### Changed
- none

### Removed
- none

### Deprecated 
- none

### Fixed
- none

### Security
- none

## [1.2.0] - 2024-02-08

### Added
- Added script to generate PKI certificates for IDS-testbed using CFSSL
- Created new IDS-testbed certificates for the deployment of the components

### Changed
- Certificate Authority using CFSSL
  - CA certificates include IP Addresses and Subject Alternative Names to the created certificates
- Changed IDS-testbed component's certificates with new CA provided certificates
- Updated `CertificateAuthority/README.md` file to highlight the new CFSSL process for certificate generation
- Updated docker-compose nginx version to 1.25.3 and proper certificate used names

  
## [1.1.0] - 2022-12-15

### Added
- Introduced a PostgreSQL database for each connector
- Created new IDS-testbed certificates for the deployment of the components

### Changed
- Updated component versions:
  - DSC version 7.1.0 --> 8.0.2
  - MDB version 5.0.0 --> 5.0.3 
- Updated CA to include IP Addresses and Subject Alternative Names to the created certificates
- Changed IDS-testbed component's certificates with new CA provided certificates
- Updated `InstallationGuide.md` and `README.md` files
- Updated `CertificateAuthority/README.md` file


## [1.0.4] - 2022-11-18

### Changed
- Added `Hardware requirements` and `Stop and delete IDS-testbed set up` sections to `InstallationGuide.md` file 
- Added `IDS Reference Testbed Implementations` to `README.md` file

### Fixed
- Fix error at `InstallationGUide.md` Index
- Fix consistency in CA and SubCA certificate generation


## [1.0.3] - 2022-09-15

### Added
- Included Index at `InstallationGuide.md` file

### Changed
- Updated `InstallationGuide.md`, `README.md`, `TestbedUserGuide.md` and  file
- Updated MetadataBroker to version 5.0.0

### Deprecated 
- Version 4.2.8 of MetadataBroker due to errors with the local broker-core image

### Fixed
- Fix documentation files to apply DAPS production set up
- Fix `register_connector.sh` script


## [1.0.2] - 2022-08-25

### Added
- Set up Omejdn DAPS production version 1.6.0 (folder `DAPS`)
- Added TLS certificates for the DAPS `https` communication protocol

### Changed
- Change DAPS from `http` to `https`
- Modified `.env` and `DAPS/nginx.conf` files
-  DSC and MDB dependencies updated to use `https` DAPS
- Modified `docker-compose.yml` file to apply changes
- Artifact content in postman collection
- Updated `MVDS.md` file

### Removed
- Removed unnecessary certificates from `DAPS` folder

### Fixed
- Fix Postman Collection invalid JSON for artifact


## [1.0.1] - 2022-07-13
### Added
- Added automated testsuite (folder `Testsuite`)
- Created `minimum-viable-data-space` folder

### Changed
- Updated `minimum-viable-data-space/MVDS.md` file

### Fixed
- Fix Postman link in ‘README.md` file


## [1.0.0] - 2022-05-25

### Added
- Provide a Certificate Authority (folder `CA`) with generated PKI certificates (CA, SubCA and Cert) used in the deployment of IDS-testbed components
- Set up Omejdn DAPS development version 1.6.0 (folder `DAPS`)
- Set up Dataspace Connector version 7.1.0 (folder `DataspaceConnectorA` and `DataspaceConnectorB`)
- Set up Metadata Broker version 4.2.8 (folder `MetadataBroker`)
- Provide a documentation file for setting up the IDS-testbed (file `InstallationGuide.md`)
- Provided a `Postman Collection` to execute the generation of a complete offer at DSCA, registration of both connectors at the Metadata Broker and a complete contract negotiation between connectors
