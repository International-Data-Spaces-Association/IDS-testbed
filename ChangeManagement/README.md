# Change Management

The change management process focuses on four pillars: understanding the change to be made, planning this change, implementing the change, and communicating the change. There can be several reasons for the need to change the Testbed, such as, a new component version, changes in the component requirements that the environment or the component must meet, and changes in the Information Model. This document will cover the process in detail to successfully proceed to include changes into the Testbed.

## Including a new component or an existing component’s new version into the Testbed

The Testbed will serve as the environment where collaborators can work on the interoperability between components within the IDSA environment. The Testbed with the validated components and their respective versions will be available in the following Github repository (https://github.com/International-Data-Spaces-Association/IDS-testbed/tree/master/Testbed).

The following procedure will be followed for the inclusion of new components and/or component versions for the different components in the Testbed.

The new component and/or component version will be isolated with respect to the Testbed directory. The component’s code quality, documentation and functionality will be evaluated. Every modification and/or new feature(s) with respect to previous versions, if any, must be clearly documented.

Once the modification and/or new feature(s) have been verified, interoperability will be tested in a trial Testbed. Here, the component will be slowly connected to the other components to ensure that functionality remains as expected until the trial Testbed includes every previously included component. 

The list of points that are checked:

•	Installation must work (installation manual, user manual, etc.)

•	Documentation must exists

•	~~Security profile requirements of the component are fulfilled~~

•	~~Code quality~~

•	~~Operational procedures~~

•	~~Interoperability functionality~~

@SQS: *How do you want to do that? Example, Code quality When it comes to code quality, there are an infinite number of evaluation options.*

**Testing a component** 

1. Testbed is up and running
2. The existing testbed is expanded to include a component to be tested.
3. The new component will be placed in a new isolation directory (VM).  This directory will be the trial Testbed area for the new component versions.
4. Documentation of the new component will be checked
5. Functionality of the new component will be checked, like described in the documentation 
6. If the new component and/or component version successfully interacts with the trial Testbed, it is ready to be moved into the Testbed.

**Testbed Updates** 

The updates that occur in the Testbed must be included in an update log where the changes, dates, and team in charge of validation will be detailed.

If a new component and/or component version demands changes in the installation and/or interoperability document of the Testbed, the new documents must be available and reference the Testbed version they apply to.

Operating system updates are always carried out (VM).

Before new components are inserted into the IDS testbed and thus used as the basis for all further tests, central communication must take place with all test facilities.

@WHO will do this?

## Criterion changes/updates

Components affected by the changes directly (affects component’s funcionality/security) or indirectly (require extra features to accept new funcionality/security from other components) should be notified. The components will have to upgrade their components to meet the new requirements and go through the “Including a new component or an existing component’s new version into the Testbed” section of this document.

**Important:** If major criterion changes were to occur that may cause those non-updated components to disrupt the expected behavior of the IDSA architecture, the Testbed maintainers may be able to disable/remove such components temporarily.

## Changes in the Information Model

The TestBed environment and the validated components will have an identifier associated with the version of the Information Model to which it applies.

When significant changes in the IDS Information Model occur, they will be clearly reflected in the Information Model’s Github repository.  

The release process will always be aligned with the IDSA architecture, and the new validated Information Model for the TestBed will be indicated in the corresponding release of the TestBed.

## Software Updates

The components must keep up with software dependencies as updates come out. The component must not force the user to skip updates to be able to keep utilizing the hardware/software. In case of changes affecting the functionality of the component, the developers must provide new installation and/or configuration steps.


