# IDS-testbed testsuite (wip)

## Set-up
**The Testbed must be set-up and running**. Please follow the instructions in the [installation guide](../InstallationGuide.md) and come back.

Currently, it is possible to run tests again an IDS connector and IDS broker. Simply run the following commands and replace $COMPONENT with either "Connector" or "Broker":

`cd Testsuite`

`docker build --network host -t test . --build-arg component=$COMPONENT`

## Covered IDS certification criteria (Connector) for Testbed v1.1
Last update: **May 06th, 2022**

| Criterion  | Covered  | Comment  |
| :---:      | :-:      | :-:      |
| COM_01     |   Yes    | implicitly through allowing only TLS v.1.2 and v.1.3 |
| COM_02     |   Yes    | implicitly through successful connection establishment (requesting DAT everytime) |
| COM_03     |   Yes    | implicitly through allowing only TLS v.1.2 and v.1.3 |
| USC_01     |   Yes     |          |
| USC_02     |   Yes     |          |
| INF_01     |   Yes     | SUT is able to send a self-description |
| INF_03     |   Partly | Currently, the sent self description do not define connector operator and log format of data endpoints offered |
| INF_04     |   Yes     | Implicitly covered by successful message exchange (additional error cases later) |
| INF_05     |   Yes     |    SUT is able to send messages with DAT   |  
| IAM_01     |   Yes     |   The DAT sent by the SUT is valid and requested everytime the connection is established       |
| IAM_04     |   No     |          |
| BRK_01     |   Yes     |          |
| BRK_02     |   Partly     |          |
| CR\_1_2    |   No     |          |
| CR\_1_8    |   No     |          |
| CR\_1_9    |   No     |          |
| CR\_3_1    |   Yes     | implicitly through allowing only TLS v.1.2 and v.1.3   |
| CR\_3\_1_1 |   Yes     | implicitly through allowing only TLS v.1.2 and v.1.3   |

**Complete: 77% (14/18 criteria)**
