# Versions of Testbed

## First version of the testbed

Minimal viable data space with essential and already available components
![first_version](./pictures/Testbed_1.0.png)

## Next version of the testbed

Minimal viable data space with all intended components
![next_version](./pictures/Testbed_1.X.png)

## Final vision for the testbed

On the long run, the testbed should be equipped with a test suite and testing components replacing the  open source reference implementations of the components.
![vision](./pictures/Testbed_vision.png)

## Feedback
First version of the testbed:
* Change connector B's text for clarity: Connector B (acting as Data Consumer) -> solved
* How does the OSCP Server implementation look? Is this something new to be installed for this current testbed? -> an OCSP provider is typically a part of the CA offering, the purpose is to enable revocation of issued certificates by offering a server with which an online status check can be conducted

Final vision for the testbed:
* We should add the AppStore to the final vision of the testbed as part of the IDSA infrastructure -> solved
