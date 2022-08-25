# What is a Minimum Viable Data Space? 
A minimum viable data space (MVDS) is a combination of components to initiate a data space with just enough features to be usable for secure and sovereign data exchange, as specified by IDSA. 
It aims to facilitate the work of experimenters by shortening the implementation time (by avoiding lengthy details that would slow down the first release). 
This allows them to start with a first working version (where secure and sovereign data exchange is granted), where the development team can iterate, identify and respond to the assumptions about the requirements of the data space. 

The MVDS is the unique solution provided by IDSA Head Office, as current best practice.

The [IDS Deployment Scenarios](https://github.com/International-Data-Spaces-Association/IDS-Deployment-Scenarios) act as best practices and sources of inspiration on building data spaces. There, you will find various examples of implementation, along with experiments also run with MVDS.

To find more information on implementing data spaces and a step-by-step classification of existing IDS documentation, you may check [How to Build Data Spaces?](https://github.com/International-Data-Spaces-Association/idsa/tree/main/how-to-build-data-spaces). 

# What are the components that make a MVDS?
A minimum viable data space consists of: 
1. Two or more IDS connectors  
2. The [Certificate Authority (CA)](https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/IDS-testbed-mvds/CertificateAuthority) granting X.509 certificates (not to be confused with certification)
3. The [Dynamic Attributes Provisioning Service (DAPS)](https://github.com/International-Data-Spaces-Association/omejdn-daps) to handle dynamic attributes and manage dynamic access tokens
4. [MetadataBroker](https://github.com/International-Data-Spaces-Association/metadata-broker-open-core) which is a registry for IDS Connector self-description documents.

![Minimum Viable Data Space](../pictures/MVDS-Testbed_1.0.png)

[Certification](https://internationaldataspaces.org/use/certification/) of all components and the operational environments is an additional trust layer, since it ensures the functionality of components work in clearly specified boundaries.

# How can I start experimenting with a MVDS? 
To start with implementing a MVDS, you can check the list of IDS-compliant components that are listed [on this page](https://github.com/International-Data-Spaces-Association/idsa/blob/main/how-to-build-data-spaces/3-Build-Components.md), which is part of the
[How to Build Data Spaces?](https://github.com/International-Data-Spaces-Association/idsa/tree/main/how-to-build-data-spaces) section, that explains the process of building data spaces in five steps.

:arrow_forward: And we recommend to use [IDS-testbed](https://github.com/International-Data-Spaces-Association/IDS-testbed) to ensure the compatibility and interoperability of the components you will be using in your MVDS.
