# IDS-testbed Testsuite

## Set-up
**The Testbed must be set-up and running**. Please follow the instructions in the [installation guide](../InstallationGuide.md) and come back.

Please ensure, you have already installed. If not, please use the following command (depending on you OS):

`sudo apt install htmldoc`

In addition, you need a running [NodeJs installation](https://nodejs.org/en/download/).

You install the missing modules by running the following commands (probably with `sudo`):

`npm install -g newman`

`npm install -g newman-reporter-html`

## Configuration file
There are a couple of environmental variables to be set before running the test suite. Because the configuration differs between IDS components, it is necessary to change at least the following values (whereas the others might be common between different IDS components).

### Connector environment variables
Navigate to the file `Testsuite/env/Applicant_IDS_Connector_Test_Configuration.postman_environment.json` and open it in an editor of choice.

Replace the **value of the JSON "value"** at least of the following environment variables - if necessary - so it fits to your IDS Connector configuration:
```json
{
  "key": "APPLICANT_CONNECTOR_PROTOCOL",
  "value": "https",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_CONNECTOR_IP_DNS",
  "value": "connectorb",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_CONNECTOR_IP",
  "value": "localhost",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_CONNECTOR_PORT",
  "value": "8081",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_MAINTAINER",
  "value": "https://sovity.de/",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_SECURITY_PROFILE",
  "value": "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_CONNECTOR_ID",
  "value": "https://connector_B",
  "type": "default",
  "enabled": true
}
```
In addition, please check whether you need to adjust the following values according to your component. The IDS Testsuite uses some default paths to resources known in the IDS, e.g. `/api/ids/data`. If you need to change those too, please change the environment variables accordingly.

Normally, every resource is represented by one environment variable as shown in following extract:

```json
{
  "key": "APPLICANT_FIRST_LEVEL_RESOURCE_NAME",
  "value": "/api",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_IDS_RESOURCE_NAME",
  "value": "/ids",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_DATA_RESOURCE_NAME",
  "value": "/data",
  "type": "default",
  "enabled": true
},
{
  "key": "APPLICANT_CONNECTOR_ACCESS_URL",
  "value": "{{APPLICANT_FIRST_LEVEL_RESOURCE_NAME}}{{APPLICANT_IDS_RESOURCE_NAME}}{{APPLICANT_DATA_RESOURCE_NAME}}",
  "type": "default",
  "enabled": true
}
```

## Execution
Currently, it is possible to run tests again an IDS Connector and IDS Broker. Simply run the following commands and replace $COMPONENT with either "Connector" or "Broker":

`cd Testsuite`

`sh execute.sh $COMPONENT`

The PDF report can be found in the results folder.

## Covered IDS certification criteria (Connector) for Testbed v1.1
Last update: **May 30th, 2022**

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
| BRK_01     |   Yes     |          |
| BRK_02     |   Partly     |          |
| CR\_3_1    |   Yes     | implicitly through allowing only TLS v.1.2 and v.1.3   |
| CR\_3\_1_1 |   Yes     | implicitly through allowing only TLS v.1.2 and v.1.3   |
