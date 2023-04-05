# What is a Minimum Viable Data Space? 
A Minimum Viable Dataspace (MVDS) is a combination of components that enable the creation of a Dataspace with just enough features to be usable for secure and sovereign data exchange, as specified by the International Dataspaces Association (IDSA). The goal of an MVDS is to streamline the implementation process, making it easier and faster for experimenters to create a working Dataspace with secure and sovereign data exchange. By starting with an MVDS, the development team can iterate quickly and respond to the requirements of the Dataspace, making adjustments as necessary to meet the needs of users.

It aims to facilitate the work of experimenters by shortening the implementation time (by avoiding lengthy details that would slow down the first release). This allows them to start with a first working version (where secure and sovereign data exchange is granted), where the development team can iterate, identify and respond to the assumptions about the requirements of the data space. 

The MVDS is the unique solution provided by IDSA Head Office, as current best practice.

The [IDS Deployment Scenarios](https://github.com/International-Data-Spaces-Association/IDS-Deployment-Scenarios) act as best practices and sources of inspiration on building data spaces. There, you will find examples of implementation, along with experiments also run with MVDS.

# What are the components required for a MVDS (as specified by IDSA)?
A minimum viable data space consists of: 
1️⃣ Two connectors (one acting as a data provider, and one as a data consumer)
2️⃣ An identity provider (Dynamic Attribute Provisioning Service, Certificate Authority)
3️⃣ Optional and additional components, such as a metadata broker, an app store, a clearing house, or a vocabulary provider, can be added to the MVDS to extend its functionality and enable more advanced features, such as searching for data sets. 

The MVDS provides a starting point for experimenters to create a functional Dataspace that can be customized and expanded as needed to meet specific requirements.

![Minimum Viable Data Space](../pictures/MVDS-Testbed_1.0.png)

[Certification](https://internationaldataspaces.org/use/certification/) of all components and the operational environments is an additional trust layer, since it ensures the functionality of components work in clearly specified boundaries.

# How can I start implementing a MVDS? 
To implement a Minimum Viable Data Space, one has two paths to follow: 
- Should start by checking the available reusable components

:arrow_forward: And we recommend to use [IDS-testbed](https://github.com/International-Data-Spaces-Association/IDS-testbed) to ensure the compatibility and interoperability of the components you will be using in your MVDS.


# Are the IDS Reference Testbed and MVDS the same thing? 


