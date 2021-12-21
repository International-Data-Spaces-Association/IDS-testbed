# Testbed User Guide

## Purpose
This user guide is meant to explain to each testbed user what they should do with the testbed in order to assess the compatibility of their own developed component.

## Steps for testing your connector

### 1. Initial preparation
#### 1.1. Download the testbed and install it
Follow the instructions in the [installation and configuration guide](./README.md) to install and configure the testbed as required.
The easiest way for the checklist approach would be to use the preconfigured setup described or get in tough with someone who already has such a testbed up and running.

### 2. Integrating your connector into the ecosystem
#### 2.1. Generate a certificate for your connector
Generate a private-public key pair for your connector.
Issue a certificate for the public key in this key pair signed by the private key of the utilized testbed CA:
```bash
./pki.py cert sign --key-file [path to public key file] --subCA [Sub CA name] --common-name "example.com" --client
```
(TODO Monika: add sub CA name utilized in preconfigured setup as soon as its available, define in which folder the command needs to be utilized)
Ensure that your connector always utilizes this IDS certificate to prove their identity with respect to the other components.

#### 2.2. Configure your connector
* Configure the usage of the Root CA (cert) to be found in (TODO Monika: add path utilized in preconfigured setup once available as soon as its available)
* Configure your connector to use the DAPS available under http://localhost:4567 (endpoints: /token, /.well-known/jwks.json)
* Provide a self-description for your connector

### 3. Interacting with the DAPS
#### 3.1. Register your connector at the DAPS
Register your connector following
a) the instructions provided here:
https://github.com/International-Data-Spaces-Association/omejdn-daps#registering-connectors
or b) the manual steps described below:
TODO SQS: explain what to do

#### 3.2. Request your DAT
* Use your connector to request a DAT from the DAPS
* Validate that you received a valid DAT corresponding to the specification:
https://github.com/International-Data-Spaces-Association/IDS-G/blob/main/Components/IdentityProvider/DAPS/README.md#dynamic-attribute-token-dat

### 4. Interacting with connectors
#### 4.1. Request self-descriptions from available connectors
***Connector A***
Connector A is available at the following URL: https://localhost:8080

Request the Self-Description from Connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - still work in progress TODO: remove as soon as it works
  * IDS-REST: not yet supported by connector A

Validate that you receive the following self-description: ```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  }
{
  "@type" : "ids:BaseConnector",
  "@id" : "https://connector_A",
  "ids:version" : "6.2.0",
  "ids:description" : [ {
    "@value" : "IDS Connector A with static example resources",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Dataspace Connector",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:hasEndpoint" : [ ],
  "ids:hasDefaultEndpoint" : {
    "@type" : "ids:ConnectorEndpoint",
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "ids:accessURL" : {
      "@id" : "https://connectora:8080/api/ids/data"
    },
    "ids:endpointDocumentation" : [ ],
    "ids:endpointInformation" : [ ]
  },
  "ids:resourceCatalog" : [ {
    "@type" : "ids:ResourceCatalog",
    "@id" : "https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f",
    "ids:offeredResource" : [ ],
    "ids:requestedResource" : [ ]
  } ],
  "ids:hasAgent" : [ ],
  "ids:securityProfile" : {
    "@id" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE"
  },
  "ids:extendedGuarantee" : [ ],
  "ids:maintainer" : {
    "@id" : "https://www.isst.fraunhofer.de/"
  },
  "ids:curator" : {
    "@id" : "https://www.isst.fraunhofer.de/"
  },
  "ids:inboundModelVersion" : [ "4.2.0", "4.1.2", "4.0.0", "4.1.0" ],
  "ids:outboundModelVersion" : "4.2.0",
  "ids:publicKey" : {
    "@type" : "ids:PublicKey",
    "@id" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "ids:keyType" : {
      "@id" : "https://w3id.org/idsa/code/RSA"
    },
    "ids:keyValue" : "VFVsSlFrbHFRVTVDWjJ0eGFHdHBSemwzTUVKQlVVVkdRVUZQUTBGUk9FRk5TVWxDUTJkTFEwRlJSVUYxZHpadFJuSmtabXhZV2xSS1owWlBRVFZ6YlVSWVF6QTVVMjF3U2xkdlIzQjVSVkphVGtWNU16RndTMlJ6VWtkb1ZHbHdVakkzYWpscGNtMXRjV2xvZGpkblNXZDZRMjU0Tm10SlVrNUhTVEoxTUc5R1VUVkdaM1pQTVhoNFozcGphV2hrY0VZd1EyaGxUMlk1U1U1bmFYTlFhM0UxYUdvNFFXVXZSRmxZYTNacWFGRTJZelpoYXk5YVdXWnFNRTV3Y1hsRlVHTktOVTFNVW0xWlIyVjRUV0ZOV20xVVluRkVTblpLYkRWS1J6TXJZa1V6V1dFeU1XaFVXbGxQZUdsVGFXTndaa1puU2pNd2EyNDFZVlZKUVhSa01EVkpXbmszZWpGelJHbFdUSFJVV0d4TVptVXZXbEZETkhCdWFrWjBjeXQwWXpFeWMxZzVhV2hKYlc1RGEyUXdWM1o2TTBOVVdtOTVRbE56WXpGVVpFSnJZamx0TUVNMWRIWm5NR1pSVURSUlowWXZla2d5VVc5YWJtNXlTVFV5ZFVGYU9FMXZiVmQwV1RKc2RETkVNR3RyY0ZJMk9YQm1Wa1JLTjNremRrNHZaWGRKUkVGUlFVST0="
    }
  }
}
```

***Connector B***
Connector B is available at the following URL: https://localhost:8081

Request the Self-Description from Connector B using those of the following protocols that you support::
  * Multipart: currently supported by connector B
  * IDSCP2: currently supported by connector B - still work in progress TODO: remove as soon as it works
  * IDS-REST: not yet supported by connector B

Validate that you receive the following self-description:
[TODO SQS: add link to the self-description]

#### 4.2. Request data sets from available connectors
***Connector A***
Connector A offers a data artifact "Hello world". Obtain the resource catalog @id from the Self-Description requested in 4.1
> https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f

Request the "hello world" data sets from connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - still work in progress TODO: remove as soon as it works
  * IDS-REST: not yet supported by connector A

Validate that you receive the data set containing the "Hello world" data.

***Connector B***
Connector B offers a data artifact "Goodbye world". Obtain the resource catalog @id from the Self-Description requested in 4.1
> https://connectorb:8080/api/catalogs/TODO

* Request the Self-Description from Connector B using those of the following protocols that you support::
  * Multipart: currently supported by connector B
  * IDSCP2: currently supported by connector B - still work in progress TODO: remove as soon as it works
  * IDS-REST: not yet supported by connector B
* Validate that you receive the following data set: [TODO SQS: add link to the goodbye world dataset]

### 5. Interacting with the MetaData Broker
### 5.1. Query the self-description of the MetaData Broker
Request the self-description of the Broker.

The response body should give code 200 and should be comparable to the following:
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Broker",
  "@id" : "https://localhost/",
  "ids:description" : [ {
    "@value" : "A Broker with a graph persistence layer",
    "@language" : "en"
  } ],
  "ids:title" : [ {
    "@value" : "IDS Metadata Broker",
    "@language" : "en"
  } ],
  "ids:maintainer" : {
    "@id" : "https://www.iais.fraunhofer.de"
  },
  "ids:curator" : {
    "@id" : "https://www.iais.fraunhofer.de"
  },
  "ids:inboundModelVersion" : [ "4.0.3" ],
  "ids:outboundModelVersion" : "4.0.3",
  "ids:resourceCatalog" : [ {
    "@type" : "ids:ResourceCatalog",
    "@id" : "https://w3id.org/idsa/autogen/resourceCatalog/b76c559b-359d-4e2f-bb2e-cd2692ed985e",
    "ids:offeredResource" : [ {
      "@type" : "ids:DataResource",
      "@id" : "https://w3id.org/idsa/autogen/dataResource/41ea3278-89ed-4e5a-800c-836f01731dbd",
      "ids:description" : [ ],
      "ids:language" : [ ],
      "ids:title" : [ ],
      "ids:resourceEndpoint" : [ ],
      "ids:contractOffer" : [ ],
      "ids:sample" : [ ],
      "ids:contentPart" : [ ],
      "ids:representation" : [ {
        "@type" : "ids:DataRepresentation",
        "@id" : "https://w3id.org/idsa/autogen/dataRepresentation/44c079d3-0e53-40d3-8634-0a15f424e459",
        "ids:description" : [ ],
        "ids:title" : [ ],
        "ids:instance" : [ {
          "@type" : "ids:Artifact",
          "@id" : "https://localhost/connectors/"
        } ]
      } ],
      "ids:defaultRepresentation" : [ ],
      "ids:resourcePart" : [ ],
      "ids:theme" : [ ],
      "ids:keyword" : [ ],
      "ids:temporalCoverage" : [ ],
      "ids:spatialCoverage" : [ ]
    } ],
    "ids:requestedResource" : [ ]
  } ],
  "ids:hasAgent" : [ ],
  "ids:securityProfile" : {
    "@id" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE"
  },
  "ids:extendedGuarantee" : [ ],
  "ids:hasEndpoint" : [ {
    "@type" : "ids:ConnectorEndpoint",
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/2d4cf2c8-2023-4e5e-8f3e-0906ba278295",
    "ids:path" : "/infrastructure",
    "ids:accessURL" : {
      "@id" : "https://localhost/infrastructure"
    },
    "ids:endpointDocumentation" : [ {
      "@id" : "https://app.swaggerhub.com/apis/idsa/IDS-Broker/1.3.1#/Multipart%20Interactions/post_infrastructure"
    } ],
    "ids:endpointInformation" : [ {
      "@value" : "This endpoint provides IDS Connector and IDS Resource registration and search capabilities at the IDS Metadata Broker.",
      "@language" : "en"
    }, {
      "@value" : "Dieser Endpunkt ermÃ¶glicht die Registrierung von und das Suchen nach IDS Connectoren und IDS Ressourcen am IDS Metadata Broker.",
      "@language" : "de"
    } ]
  } ],
  "ids:hasDefaultEndpoint" : {
    "@type" : "ids:ConnectorEndpoint",
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/ac1ce4c6-6e8a-490d-bf7d-2ae560fd7ba5",
    "ids:path" : "/",
    "ids:accessURL" : {
      "@id" : "https://localhost/"
    },
    "ids:endpointDocumentation" : [ ],
    "ids:endpointInformation" : [ {
      "@value" : "Dieser Endpunkt liefert eine Selbstbeschreibung dieses IDS Connectors",
      "@language" : "de"
    }, {
      "@value" : "Endpoint providing a self-description of this connector",
      "@language" : "en"
    } ]
  },
  "ids:connectorCatalog" : [ ]
}
```

#### 5.2. Query the MetaData Broker for available data in the testbed
The MetaData Broker can be reached at https://localhost[:443] and is aware of the self-descriptions of connector A and B.

* Query the MetaData Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker
* Validate that you received the following response:
[TODO SQS: add (link to) the expected output]

#### 5.3. Register your connector at the IDS MetaDataBroker with an exemplary data set
* Register your connector at the MetaData Broker using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker
* Query the MetaData Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker
* Validate that you received the following response:
[TODO SQS: add the expected output + your own entered information]

### In addition: Execute Test suite

Please execute the automated (interoperability) test suite provided at https://gitlab.cc-asp.fraunhofer.de/ksa/ids-certification-testing by following the installation and exection instructions in that repository.
