# Example for Executing Testbed User Guide with a Dataspace Connector

## Purpose
This user guide is meant to explain to each testbed user the structure of the different calls and the response body they should obtain from these calls when they generate a resource in their connector, upload their connector to the broker, negotiate between connectors or search for a connector in the broker, in order to assess the compatibility of their own developed component.

## 4.1. Request self-description from Connector A


## 4.2. Request data from Connector A
This section will explain the necessary steps that need to be follow in order to request data from Dataspace Connector A using a Dataspace Connector C.
The following steps have been extracted from the official documentation guide (https://international-data-spaces-association.github.io/DataspaceConnector/CommunicationGuide/v6/Consumer) and have been modified in order to work for the reference Testbed deployment setup.

With the Reference TestBed setup, follow the next steps on Dataspace Connector C to obtain data from Dataspace Connector A. Access in a browser https://localhost:8081 and enter in the Swagger UI of connector C.

### 4.2. Request self-description from Connector A
Request the self-description of Connector A utilizing POST /api/ids/description.

Insert the recipient url of Connector A
> https://connectora:8080/api/ids/data

Left the elementId remains empty because the id of the requested resource is not known .

The response body should give code 200 and should be comparable to the one provided in the [Testbed User Guide](./TestbedUserGuide.md).

From this Response body obtain the resource catalog @id
> https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f

### 4.2. Request data set from Connector A
#### Request Information regarding the desired resource
Request the specific resource catalog of Connector A using POST /api/ids/description

Insert the recipient url of Connector A (recipient)
> https://connectora:8080/api/ids/data

Insert the id of the requested resource (elementId)
> https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f

The response body should give code 200 and should have this structure:
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:ResourceCatalog",
  "@id" : "https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f",
  "ids:offeredResource" : [ {
    "@type" : "ids:Resource",
    "@id" : "https://connectora:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991",
    "ids:description" : [ {
      "@value" : "DWD weather warnings for germany.",
      "@language" : "DE"
    } ],
    "ids:version" : "1",
    "ids:language" : [ {
      "@id" : "https://w3id.org/idsa/code/DE"
    } ],
    "ids:title" : [ {
      "@value" : "DWD Weather Warnings",
      "@language" : "DE"
    } ],
    "ids:publisher" : {
      "@id" : "https://dwd.com"
    },
    "ids:sovereign" : {
      "@id" : "https://dwd.com"
    },
    "ids:resourceEndpoint" : [ {
      "@type" : "ids:ConnectorEndpoint",
      "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/e3e0fb5a-f0bd-4c29-8200-f6c8e138d04c",
      "ids:accessURL" : {
        "@id" : "https://connectora:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991"
      },
      "ids:endpointDocumentation" : [ {
        "@id" : ""
      } ],
      "ids:endpointInformation" : [ ]
    } ],
    "ids:contractOffer" : [ {
      "@type" : "ids:ContractOffer",
      "@id" : "https://connectora:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea",
      "ids:permission" : [ {
        "@type" : "ids:Permission",
        "@id" : "https://connectora:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802",
        "ids:description" : [ {
          "@value" : "provide-access",
          "@type" : "http://www.w3.org/2001/XMLSchema#string"
        } ],
        "ids:title" : [ {
          "@value" : "Example Usage Policy",
          "@type" : "http://www.w3.org/2001/XMLSchema#string"
        } ],
        "ids:postDuty" : [ ],
        "ids:assignee" : [ ],
        "ids:assigner" : [ ],
        "ids:preDuty" : [ ],
        "ids:action" : [ {
          "@id" : "https://w3id.org/idsa/code/USE"
        } ],
        "ids:constraint" : [ ]
      } ],
      "ids:provider" : {
        "@id" : "https://connectora:8080/"
      },
      "ids:consumer" : {
        "@id" : ""
      },
      "ids:contractEnd" : {
        "@value" : "2023-10-22T07:48:37.068Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:contractStart" : {
        "@value" : "2021-12-16T10:24:10.230Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:contractDate" : {
        "@value" : "2021-12-16T13:20:30.521Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:prohibition" : [ ],
      "ids:obligation" : [ ]
    } ],
    "ids:sample" : [ ],
    "ids:contentPart" : [ ],
    "ids:representation" : [ {
      "@type" : "ids:Representation",
      "@id" : "https://connectora:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d",
      "ids:description" : [ ],
      "ids:language" : {
        "@id" : "https://w3id.org/idsa/code/EN"
      },
      "ids:mediaType" : {
        "@type" : "ids:IANAMediaType",
        "@id" : "https://w3id.org/idsa/autogen/iANAMediaType/bbd0d6d4-0fb2-4d68-a941-6bef561a334f",
        "ids:filenameExtension" : "json"
      },
      "ids:title" : [ ],
      "ids:instance" : [ {
        "@type" : "ids:Artifact",
        "@id" : "https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6",
        "ids:fileName" : "Artifact",
        "ids:creationDate" : {
          "@value" : "2021-12-16T10:35:37.850Z",
          "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
        },
        "ids:byteSize" : 24,
        "ids:checkSum" : "2302133775"
      } ],
      "ids:representationStandard" : {
        "@id" : ""
      },
      "ids:modified" : {
        "@value" : "2021-12-16T10:37:10.751Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:created" : {
        "@value" : "2021-12-16T10:37:10.751Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      }
    } ],
    "ids:defaultRepresentation" : [ ],
    "ids:resourcePart" : [ ],
    "ids:standardLicense" : {
      "@id" : ""
    },
    "ids:theme" : [ ],
    "ids:modified" : {
      "@value" : "2021-12-15T15:50:58.703Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    },
    "ids:keyword" : [ {
      "@value" : "DWD",
      "@language" : "DE"
    } ],
    "ids:temporalCoverage" : [ ],
    "ids:spatialCoverage" : [ ],
    "ids:created" : {
      "@value" : "2021-12-15T15:50:58.703Z",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
    }
  } ],
  "ids:requestedResource" : [ ]
}
```

From this response body it is obtained the necessary information in order to negotiate the contract (Connector A: offer id, artifact id, rule id)
> https://connectora:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991
>
> https://connectora:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802
>
> https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6

#### Start negotiation
Send IDS contract request message POST /api/ids/contract.

Insert the recipient url of Connector A (recipient)
> https://connectora:8080/api/ids/data

Insert the ids resource that should be requested (resourceIds)
> https://connectora:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991

Insert the artifact that should be requested (artifactIds)
> https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6

Put the connector automatically download data of an artifact to False (download)
> false

The request body must contain a contract offer, this contract must match the one the resource was created with. Therefore, it is a policy with a USE permission. The rule list will be automatically turned into a contract request to then send it to the provider. This will read this contract request, compare it to the artifact’s (respectively the corresponding resource’s) contract offers, and return either a ContractRejectionMessage or a ContractAgreementMessage.

Include in it the id of the Connector A rule and as ids:target the artifact.
```json
 [ {
        "@type" : "ids:Permission",
        "@id" : "https://connectora:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802",
        "ids:description" : [ {
          "@value" : "provide-access",
          "@type" : "http://www.w3.org/2001/XMLSchema#string"
        } ],
        "ids:title" : [ {
          "@value" : "Example Usage Policy",
          "@type" : "http://www.w3.org/2001/XMLSchema#string"
        } ],
        "ids:action" : [ {
          "@id" : "https://w3id.org/idsa/code/USE"
        }],
        "ids:target" : "https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6"
} ]
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T13:27:38.959+0000",
  "modificationDate": "2021-12-16T13:27:38.959+0000",
  "remoteId": "https://connectora:8080/api/agreements/01c29b32-b202-46d3-ade1-e9401b43ed0f",
  "confirmed": true,
  "value": "{\n  \"@context\" : {\n    \"ids\" : \"https://w3id.org/idsa/core/\",\n    \"idsc\" : \"https://w3id.org/idsa/code/\"\n  },\n  \"@type\" : \"ids:ContractAgreement\",\n  \"@id\" : \"https://connectora:8080/api/agreements/01c29b32-b202-46d3-ade1-e9401b43ed0f\",\n  \"ids:permission\" : [ {\n    \"@type\" : \"ids:Permission\",\n    \"@id\" : \"https://connectora:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802\",\n    \"ids:description\" : [ {\n      \"@value\" : \"provide-access\",\n      \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n    } ],\n    \"ids:title\" : [ {\n      \"@value\" : \"Example Usage Policy\",\n      \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n    } ],\n    \"ids:action\" : [ {\n      \"@id\" : \"https://w3id.org/idsa/code/USE\"\n    } ],\n    \"ids:postDuty\" : [ ],\n    \"ids:assignee\" : [ {\n      \"@id\" : \"https://connector_B\"\n    } ],\n    \"ids:assigner\" : [ {\n      \"@id\" : \"https://connector_A\"\n    } ],\n    \"ids:constraint\" : [ ],\n    \"ids:preDuty\" : [ ],\n    \"ids:target\" : {\n      \"@id\" : \"https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6\"\n    }\n  } ],\n  \"ids:provider\" : {\n    \"@id\" : \"https://connector_A\"\n  },\n  \"ids:consumer\" : {\n    \"@id\" : \"https://connector_B\"\n  },\n  \"ids:contractStart\" : {\n    \"@value\" : \"2021-12-16T13:27:37.868Z\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\n  },\n  \"ids:contractDate\" : {\n    \"@value\" : \"2021-12-16T13:27:37.868Z\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\n  },\n  \"ids:prohibition\" : [ ],\n  \"ids:obligation\" : [ ]\n}",
  "_links": {
    "self": {
      "href": "https://localhost:8081/api/agreements/3a638d21-07fb-40a0-be14-bea6d353825e"
    },
    "artifacts": {
      "href": "https://localhost:8081/api/agreements/3a638d21-07fb-40a0-be14-bea6d353825e/artifacts{?page,size}",
      "templated": true
    }
  }
}
```

From this response body it is obtained the Connector B agreement
> https://localhost:8081/api/agreements/3a638d21-07fb-40a0-be14-bea6d353825e

#### Request the artifact based on the existing agreement
To get the artifact and their data link, make the following request: POST /api/agreements/{id}/artifacts

Insert the agreement id
> 3a638d21-07fb-40a0-be14-bea6d353825e

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "artifacts": [
      {
        "creationDate": "2021-12-16T13:27:40.183+0000",
        "modificationDate": "2021-12-16T15:32:57.815+0000",
        "remoteId": "https://connectora:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6",
        "title": "Artifact",
        "description": "",
        "numAccessed": 1,
        "byteSize": 24,
        "checkSum": 2302133775,
        "additional": {
          "ids:byteSize": "24",
          "ids:creationDate": "2021-12-16T10:35:37.850Z",
          "ids:checkSum": "2302133775"
        },
        "_links": {
          "self": {
            "href": "https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d"
          },
          "data": {
            "href": "https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d/data"
          },
          "representations": {
            "href": "https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d/representations{?page,size}",
            "templated": true
          },
          "agreements": {
            "href": "https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d/agreements{?page,size}",
            "templated": true
          },
          "subscriptions": {
            "href": "https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d/subscriptions{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8081/api/agreements/3a638d21-07fb-40a0-be14-bea6d353825e/artifacts?page=0&size=30"
    }
  },
  "page": {
    "size": 30,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}
```

From this response body it is obtained the url to obtain the data
> https://localhost:8081/api/artifacts/1d06f1c0-8b64-465a-b5f0-3e185914a67d/data

If you paste it in a browser and download it is obtained the "Hello World" data.

## 5. Interacting with the MetaData Broker
In this section it will be explained how to interact with the broker and update the connector A and B to the broker.

### 5.1. Query the self-description of the MetaData broker
Obtain broker self-description utilizing Message POST /api/ids/description.

Insert the recipient url
> https://broker-localhost_broker-reverseproxy_1/infrastructure

### 5.2 Query the MetaData Broker for available data in the Testbed
### Messages POST /api/ids/description
Obtain the list of connectors at the broker.

Insert the recipient url
> https://broker-localhost_broker-reverseproxy_1/infrastructure

Insert the element id of the requested resource
> https://localhost/connectors/

The response body should give code 200 and should have this structure:
```json
{
  "@graph" : [ {
    "@id" : "https://localhost/connectors/",
    "@type" : "ids:ConnectorCatalog",
    "listedConnector" : [ "https://localhost/connectors/2129657531", "https://localhost/connectors/2129657530" ]
  }, {
    "@id" : "https://localhost/connectors/2129657530",
    "@type" : "ids:BaseConnector",
    "sameAs" : "https://connector_A",
    "curator" : "https://www.isst.fraunhofer.de/",
    "description" : "IDS Connector A with static example resources",
    "hasDefaultEndpoint" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "inboundModelVersion" : [ "4.2.0", "4.0.0", "4.1.0", "4.1.2" ],
    "maintainer" : "https://www.isst.fraunhofer.de/",
    "outboundModelVersion" : "4.2.0",
    "publicKey" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "resourceCatalog" : "https://localhost/connectors/2129657530/-733289566",
    "securityProfile" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE",
    "title" : "Dataspace Connector",
    "version" : "6.2.0"
  }, {
    "@id" : "https://localhost/connectors/2129657530/-733289566",
    "@type" : "ids:ResourceCatalog",
    "sameAs" : "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f"
  }, {
    "@id" : "https://localhost/connectors/2129657531",
    "@type" : "ids:BaseConnector",
    "sameAs" : "https://connector_B",
    "curator" : "https://www.isst.fraunhofer.de/",
    "description" : "IDS Connector B with static example resources",
    "hasDefaultEndpoint" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "inboundModelVersion" : [ "4.2.0", "4.1.0", "4.1.2", "4.0.0" ],
    "maintainer" : "https://www.isst.fraunhofer.de/",
    "outboundModelVersion" : "4.2.0",
    "publicKey" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "securityProfile" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE",
    "title" : "Dataspace Connector",
    "version" : "6.2.0"
  }, {
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "@type" : "ids:ConnectorEndpoint",
    "accessURL" : [ "https://connectorb:8081/api/ids/data", "https://connectora:8080/api/ids/data" ]
  }, {
    "@id" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "@type" : "ids:PublicKey",
    "keyType" : "https://w3id.org/idsa/code/RSA",
    "keyValue" : "VkZWc1NsRnJiSEZSVlRWRFdqSjBlR0ZIZEhCU2Vtd3pUVVZLUWxWVlZrZFJWVVpRVVRCR1VrOUZSazVUVld4RFVUSmtURkV3UmxKU1ZVWXhaSHBhZEZKdVNtdGFiWGhaVjJ4U1Mxb3dXbEJSVkZaNllsVlNXVkY2UVRWVk1qRjNVMnhrZGxJelFqVlNWa3BoVkd0V05VMTZSbmRUTWxKNlZXdGtiMVpIYkhkVmFra3pZV3BzY0dOdE1YUmpWMnh2Wkdwa2JsTlhaRFpSTWpVMFRtMTBTbFZyTlVoVFZFb3hUVWM1UjFWVVZrZGFNMXBRVFZob05Gb3pjR3BoVjJoclkwVlpkMUV5YUd4VU1sazFVMVUxYm1GWVRsRmhNMFV4WVVkdk5GRlhWWFpTUm14WllUTmFjV0ZHUlRKWmVscG9ZWGs1WVZkWFduRk5SVFYzWTFoc1JsVkhUa3RPVlRGTlZXMHhXbEl5VmpSVVYwWk9WMjB4VlZsdVJrVlRibHBMWWtSV1MxSjZUWEpaYTFWNlYxZEZlVTFYYUZWWGJHeFFaVWRzVkdGWFRuZGFhMXB1VTJwTmQyRXlOREZaVmxaS1VWaFNhMDFFVmtwWGJtc3paV3BHZWxKSGJGZFVTRkpWVjBkNFRWcHRWWFpYYkVaRVRraENkV0ZyV2pCamVYUXdXWHBGZVdNeFp6VmhWMmhLWWxjMVJHRXlVWGRXTTFvMlRUQk9WVmR0T1RWUmJFNTZXWHBHVlZwRlNuSlphbXgwVFVWTk1XUklXbTVOUjFwU1ZVUlNVbG93V1habGEyZDVWVmM1WVdKdE5YbFRWRlY1WkZWR1lVOUZNWFppVm1Rd1YxUktjMlJFVGtWTlIzUnlZMFpKTWs5WVFtMVdhMUpMVGpOcmVtUnJOSFphV0dSS1VrVkdVbEZWU1QwPQ=="
  } ],
  "@context" : {
    "accessURL" : {
      "@id" : "https://w3id.org/idsa/core/accessURL",
      "@type" : "@id"
    },
    "sameAs" : {
      "@id" : "http://www.w3.org/2002/07/owl#sameAs",
      "@type" : "@id"
    },
    "description" : {
      "@id" : "https://w3id.org/idsa/core/description"
    },
    "hasDefaultEndpoint" : {
      "@id" : "https://w3id.org/idsa/core/hasDefaultEndpoint",
      "@type" : "@id"
    },
    "publicKey" : {
      "@id" : "https://w3id.org/idsa/core/publicKey",
      "@type" : "@id"
    },
    "curator" : {
      "@id" : "https://w3id.org/idsa/core/curator",
      "@type" : "@id"
    },
    "inboundModelVersion" : {
      "@id" : "https://w3id.org/idsa/core/inboundModelVersion"
    },
    "title" : {
      "@id" : "https://w3id.org/idsa/core/title"
    },
    "outboundModelVersion" : {
      "@id" : "https://w3id.org/idsa/core/outboundModelVersion"
    },
    "securityProfile" : {
      "@id" : "https://w3id.org/idsa/core/securityProfile",
      "@type" : "@id"
    },
    "maintainer" : {
      "@id" : "https://w3id.org/idsa/core/maintainer",
      "@type" : "@id"
    },
    "resourceCatalog" : {
      "@id" : "https://w3id.org/idsa/core/resourceCatalog",
      "@type" : "@id"
    },
    "version" : {
      "@id" : "https://w3id.org/idsa/core/version"
    },
    "listedConnector" : {
      "@id" : "https://w3id.org/idsa/core/listedConnector",
      "@type" : "@id"
    },
    "keyValue" : {
      "@id" : "https://w3id.org/idsa/core/keyValue"
    },
    "keyType" : {
      "@id" : "https://w3id.org/idsa/core/keyType",
      "@type" : "@id"
    },
    "owl" : "http://www.w3.org/2002/07/owl#",
    "ids" : "https://w3id.org/idsa/core/"
  }
}
```

From here it is obtained the list of connectors and it is selected connector A element id
> https://localhost/connectors/2129657530

### Messages POST /api/ids/description
With this information it is requested Connector A information to the broker.

Insert the recipient url
> https://broker-localhost_broker-reverseproxy_1/infrastructure

Insert the element id of the requested connector A
> https://localhost/connectors/2129657530

The response body should give code 200 and should have this structure
```json
{
  "@graph" : [ {
    "@id" : "https://localhost/connectors/2129657530",
    "@type" : "ids:BaseConnector",
    "sameAs" : "https://connector_A",
    "curator" : "https://www.isst.fraunhofer.de/",
    "description" : "IDS Connector A with static example resources",
    "hasDefaultEndpoint" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "inboundModelVersion" : [ "4.2.0", "4.0.0", "4.1.0", "4.1.2" ],
    "maintainer" : "https://www.isst.fraunhofer.de/",
    "outboundModelVersion" : "4.2.0",
    "publicKey" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "resourceCatalog" : "https://localhost/connectors/2129657530/-733289566",
    "securityProfile" : "https://w3id.org/idsa/code/BASE_SECURITY_PROFILE",
    "title" : "Dataspace Connector",
    "version" : "6.2.0"
  }, {
    "@id" : "https://localhost/connectors/2129657530/-733289566",
    "@type" : "ids:ResourceCatalog",
    "sameAs" : "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f"
  }, {
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "@type" : "ids:ConnectorEndpoint",
    "accessURL" : "https://connectora:8080/api/ids/data"
  }, {
    "@id" : "https://w3id.org/idsa/autogen/publicKey/78eb73a3-3a2a-4626-a0ff-631ab50a00f9",
    "@type" : "ids:PublicKey",
    "keyType" : "https://w3id.org/idsa/code/RSA",
    "keyValue" : "VkZWc1NsRnJiSEZSVlRWRFdqSjBlR0ZIZEhCU2Vtd3pUVVZLUWxWVlZrZFJWVVpRVVRCR1VrOUZSazVUVld4RFVUSmtURkV3UmxKU1ZVWXhaSHBhZEZKdVNtdGFiWGhaVjJ4U1Mxb3dXbEJSVkZaNllsVlNXVkY2UVRWVk1qRjNVMnhrZGxJelFqVlNWa3BoVkd0V05VMTZSbmRUTWxKNlZXdGtiMVpIYkhkVmFra3pZV3BzY0dOdE1YUmpWMnh2Wkdwa2JsTlhaRFpSTWpVMFRtMTBTbFZyTlVoVFZFb3hUVWM1UjFWVVZrZGFNMXBRVFZob05Gb3pjR3BoVjJoclkwVlpkMUV5YUd4VU1sazFVMVUxYm1GWVRsRmhNMFV4WVVkdk5GRlhWWFpTUm14WllUTmFjV0ZHUlRKWmVscG9ZWGs1WVZkWFduRk5SVFYzWTFoc1JsVkhUa3RPVlRGTlZXMHhXbEl5VmpSVVYwWk9WMjB4VlZsdVJrVlRibHBMWWtSV1MxSjZUWEpaYTFWNlYxZEZlVTFYYUZWWGJHeFFaVWRzVkdGWFRuZGFhMXB1VTJwTmQyRXlOREZaVmxaS1VWaFNhMDFFVmtwWGJtc3paV3BHZWxKSGJGZFVTRkpWVjBkNFRWcHRWWFpYYkVaRVRraENkV0ZyV2pCamVYUXdXWHBGZVdNeFp6VmhWMmhLWWxjMVJHRXlVWGRXTTFvMlRUQk9WVmR0T1RWUmJFNTZXWHBHVlZwRlNuSlphbXgwVFVWTk1XUklXbTVOUjFwU1ZVUlNVbG93V1habGEyZDVWVmM1WVdKdE5YbFRWRlY1WkZWR1lVOUZNWFppVm1Rd1YxUktjMlJFVGtWTlIzUnlZMFpKTWs5WVFtMVdhMUpMVGpOcmVtUnJOSFphV0dSS1VrVkdVbEZWU1QwPQ=="
  } ],
  "@context" : {
    "accessURL" : {
      "@id" : "https://w3id.org/idsa/core/accessURL",
      "@type" : "@id"
    },
    "description" : {
      "@id" : "https://w3id.org/idsa/core/description"
    },
    "hasDefaultEndpoint" : {
      "@id" : "https://w3id.org/idsa/core/hasDefaultEndpoint",
      "@type" : "@id"
    },
    "publicKey" : {
      "@id" : "https://w3id.org/idsa/core/publicKey",
      "@type" : "@id"
    },
    "sameAs" : {
      "@id" : "http://www.w3.org/2002/07/owl#sameAs",
      "@type" : "@id"
    },
    "curator" : {
      "@id" : "https://w3id.org/idsa/core/curator",
      "@type" : "@id"
    },
    "inboundModelVersion" : {
      "@id" : "https://w3id.org/idsa/core/inboundModelVersion"
    },
    "title" : {
      "@id" : "https://w3id.org/idsa/core/title"
    },
    "outboundModelVersion" : {
      "@id" : "https://w3id.org/idsa/core/outboundModelVersion"
    },
    "securityProfile" : {
      "@id" : "https://w3id.org/idsa/core/securityProfile",
      "@type" : "@id"
    },
    "maintainer" : {
      "@id" : "https://w3id.org/idsa/core/maintainer",
      "@type" : "@id"
    },
    "resourceCatalog" : {
      "@id" : "https://w3id.org/idsa/core/resourceCatalog",
      "@type" : "@id"
    },
    "version" : {
      "@id" : "https://w3id.org/idsa/core/version"
    },
    "keyValue" : {
      "@id" : "https://w3id.org/idsa/core/keyValue"
    },
    "keyType" : {
      "@id" : "https://w3id.org/idsa/core/keyType",
      "@type" : "@id"
    },
    "owl" : "http://www.w3.org/2002/07/owl#",
    "ids" : "https://w3id.org/idsa/core/"
  }
}
```

From here it is obtained the accessURL of Connector A
> "accessURL" : "https://connectora:8080/api/ids/data"

From this point it can be followed the section "Steps from Connector B to negotiate data from Connector A" and proceed with all the steps included in that section.

#### 5.3. Register your connector at the IDS MetaDataBroker
Register  connector at the Broker with the message POST /api/ids/connector/update
#### Update Connector A at the broker.

At the Swagger UI of the Connector A insert the following recipient url.
> https://broker-localhost_broker-reverseproxy_1/infrastructure

The server response should give code 200.

#### Update Connector B at the broker.

At the Swagger UI of the Connector B insert the following recipient url.
> https://broker-localhost_broker-reverseproxy_1/infrastructure

The server response should give code 200.

Check that the Broker has both connectors in the list of connectors.
