**Process for including new versions of components into the Testbed**

The Testbed will also serve as the environment where collaborators can investigate interoperability between components within the IDSA environment.
The Github repository will have the current Testbed (https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/Testbed) with the components that have been validated and the versions used. 

The following procedure will be followed for the inclusion of new versions of the different components in the Testbed. 


The new component version will be placed in a separated folder/directory outside of the Testbed folder/directory. The component’s code quality, documentation and functionality will be tested in isolation. Every change with respect to the previous version should had been clearly documented. 

Once everything functions correctly in isolation, interoperability will be tested in a “mock” Testbed**. This will first include one other IDS component to interact with. The new version will be tested in positive and negative outcome environments to ensure everything works as it should. Slowly, more IDS components will be added to this “mock” Testbed.

The list of points that are checked:
-	Documentation must be complete (installation manual, user manual, etc.)
-	Security profile requirements of the component are fulfilled
-	Code quality
-	Operational procedures 
-	Interoperability functionality (availability of component endpoints, etc.)


Basic example: New Dataspace Connector version
The connector will be placed in a new isolation folder/directory. This folder/directory will be the quarantine area for the new component versions. The connector’s code, documentation and functionality will be checked. The DSC will act as both provider and consumer when isolated. Once the functionality works as intended, the connector will be connected to the “mock” Testbed Broker. Obtaining the DAT will not be an issue as the DSC is connected to a pre-defined DAPS. When the connector-Broker interoperability is validated, the pre-defined DAPS will be changed into the “mock” Testbed DAPS. This will be the implementation process into the “mock” Testbed. 

If the new component version successfully interacts with the whole “mock” Testbed, it is considered to be ready to be moved into the Testbed.


The updates that occur in the Testbed must be included in an update document of this where all the changes that have occurred together with the date and the person or people who have validated the new version or component are detailed.

If due to a new version of a component there are changes in the installation and interoperability document of the Testbed, the new installation and/or interoperability process of the Testbed components must be indicated in this.

** mock Testbed: closed environment used for testing the interoperability of the component’s new version with the rest of the validated components.

