# Current State of the Testbed

## Isolated components (DSC, MB, DAPS, TC)
### DataspaceConnector
Functional on its own, isolated

### Broker
Still checking some issues with the API, waiting response from developers

### DAPS
Functional on its own, isolated

### Trusted Connector
Not working (last response: "Unfortunately we do not have the capacity to offer this kind of support and I cannot help you in this case. I'm sorry and I hope you will understand.")

## Interoperable components (DSC, MB)
### DSC + MB
Currently the DSC is able to interact with the MB through the AISEC DAPS **(NOT THE DAPS IN THE TESTBED)**

### DAPS interoperability
We got a response from the author of the testbed DAPS with a work around to the issue. Currently the DAPS expects the "aud:" to be the url of the DAPS itself, but according to the IDS-Framework the "aud:" is "idsc:IDS_CONNECTORS_ALL". This is wrong from a standards perspective, but we were provided with an environment variable to overcome this.

Got an update on this matter yesterday (July 5th), working on it

**AS SOON AS THE DAPS IN INTEROPERABLE WITH THE DSC AND THE MB, SICK WILL BE NOTIFIED**
