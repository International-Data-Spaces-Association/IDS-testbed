# Preparing connector A and B

## Registering resources at connector A

First of all, this section will explain the necessary steps that need to be follow in order to have a data offered at the Dataspace Connector A of the reference TestBed (Dataspace Connector A acting as Provider).

The following steps have been extracted from the official documentation guide (https://international-data-spaces-association.github.io/DataspaceConnector/CommunicationGuide/v6/Provider) and have been modified in order to work for the reference Testbed deployment setup.

Setup the Reference TestBed and follow the next steps on Dataspace Connector A to create a complete resource. Access in a browser https://localhost:8080 and enter in the Swagger UI of connector A.

### Offered Resources POST /api/offers
Create a base resource with the following Request Body
```json
{
  "title": "Welcome",
  "description": "An exemplary resource to test transfer of data.",
  "keywords": [
    "Example"
  ],
  "publisher": "https://example.com",
  "language": "DE",
  "license": "",
  "sovereign": "https://example.com",
  "endpointDocumentation": "",
  "paymentModality": "undefined"
}
```

The response body should give code 201 and should have this structure:
TODO: update for new example resource
```json
{
  "creationDate": "2021-12-15T15:50:58.703+0000",
  "modificationDate": "2021-12-15T15:50:58.703+0000",
  "title": "Welcome",
  "description": "An exemplary resource to test transfer of data.",
  "keywords": [
    "Example"
  ],
  "publisher": "https://example.com",
  "language": "DE",
  "license": "",
  "version": 1,
  "sovereign": "https://example.com",
  "endpointDocumentation": "",
  "paymentModality": "undefined",
  "samples": [],
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991"
    },
    "contracts": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/contracts{?page,size}",
      "templated": true
    },
    "representations": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/representations{?page,size}",
      "templated": true
    },
    "catalogs": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/catalogs{?page,size}",
      "templated": true
    },
    "subscriptions": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/subscriptions{?page,size}",
      "templated": true
    },
    "brokers": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/brokers{?page,size}",
      "templated": true
    }
  }
}
```

### Catalogs POST /api/catalogs
Create a base resource with the following Request body
```json
{
  "title": "IDS Catalog",
  "description": "This catalog is created from an IDS infomodel catalog."
}
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T10:08:51.817+0000",
  "modificationDate": "2021-12-16T10:08:51.817+0000",
  "title": "IDS Catalog",
  "description": "This catalog is created from an IDS infomodel catalog.",
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f"
    },
    "offers": {
      "href": "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f/offers{?page,size}",
      "templated": true
    }
  }
}
```

### Add Offer to Catalog POST /api/catalogs/{id}/offers
Link the created offer to the created catalog.

Insert the catalog id
> 2cd59c94-54e4-4979-9842-36ee45dd354f

Use the offer as the Request body
>[
  "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991"
]

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "resources": [
      {
        "creationDate": "2021-12-15T15:50:58.703+0000",
        "modificationDate": "2021-12-15T15:50:58.703+0000",
        "title": "DWD Weather Warnings",
        "description": "DWD weather warnings for germany.",
        "keywords": [
          "DWD"
        ],
        "publisher": "https://dwd.com",
        "language": "DE",
        "license": "",
        "version": 1,
        "sovereign": "https://dwd.com",
        "endpointDocumentation": "",
        "paymentModality": "undefined",
        "samples": [],
        "additional": {},
        "_links": {
          "self": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991"
          },
          "contracts": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/contracts{?page,size}",
            "templated": true
          },
          "representations": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/representations{?page,size}",
            "templated": true
          },
          "catalogs": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/catalogs{?page,size}",
            "templated": true
          },
          "subscriptions": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/subscriptions{?page,size}",
            "templated": true
          },
          "brokers": {
            "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/brokers{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f/offers?page=0&size=30"
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

### Usage Control POST /api/examples/policy
Obtain a usage policy for provide access following the documentation (https://international-data-spaces-association.github.io/DataspaceConnector/Documentation/v6/UsageControl#example-endpoint)

Insert the following Request body
```json
{
  "title": "Example Usage Policy",
  "description": "",
  "type": "PROVIDE_ACCESS"
}
```

The response body should give code 200 and should have this structure:
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:Permission",
  "@id" : "https://w3id.org/idsa/autogen/permission/51f5f7e4-f97f-4f91-bc57-b243714642be",
  "ids:description" : [ {
    "@value" : "provide-access",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:title" : [ {
    "@value" : "Allow Data Usage",
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
}
```

### Rules POST /api/rules
Create a rule for provide access usage policy with the following Request body (it is used the provide access usage policy obtained from the previous call).
```json
{
   "title": "[\"Example Usage Policy\"^^http://www.w3.org/2001/XMLSchema#string]",
   "description": "",
   "value": "{\n  \"@context\" : {\n    \"ids\" : \"https://w3id.org/idsa/core/\",\n    \"idsc\" : \"https://w3id.org/idsa/code/\"\n  },\n  \"@type\" : \"ids:Permission\",\n  \"@id\" : \"https://w3id.org/idsa/autogen/permission/51f5f7e4-f97f-4f91-bc57-b243714642be\",\n  \"ids:description\" : [ {\n    \"@value\" : \"provide-access\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:title\" : [ {\n    \"@value\" : \"Example Usage Policy\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:postDuty\" : [ ],\n  \"ids:assignee\" : [ ],\n  \"ids:assigner\" : [ ],\n  \"ids:action\" : [ {\n    \"@id\" : \"https://w3id.org/idsa/code/USE\"\n  } ],\n  \"ids:constraint\" : [ ],\n  \"ids:preDuty\" : [ ]\n}"
}
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T10:22:02.982+0000",
  "modificationDate": "2021-12-16T10:22:02.982+0000",
  "title": "[\"Example Usage Policy\"^^http://www.w3.org/2001/XMLSchema#string]",
  "description": "",
  "value": "{\n  \"@context\" : {\n    \"ids\" : \"https://w3id.org/idsa/core/\",\n    \"idsc\" : \"https://w3id.org/idsa/code/\"\n  },\n  \"@type\" : \"ids:Permission\",\n  \"@id\" : \"https://w3id.org/idsa/autogen/permission/51f5f7e4-f97f-4f91-bc57-b243714642be\",\n  \"ids:description\" : [ {\n    \"@value\" : \"provide-access\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:title\" : [ {\n    \"@value\" : \"Example Usage Policy\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:postDuty\" : [ ],\n  \"ids:assignee\" : [ ],\n  \"ids:assigner\" : [ ],\n  \"ids:action\" : [ {\n    \"@id\" : \"https://w3id.org/idsa/code/USE\"\n  } ],\n  \"ids:constraint\" : [ ],\n  \"ids:preDuty\" : [ ]\n}",
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802"
    },
    "contracts": {
      "href": "https://localhost:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802/contracts{?page,size}",
      "templated": true
    }
  }
}
```

### Contracts POST /api/contracts
Create a contract defining the provider url, the start and end dates at the following Request body
```json
{
    "title": "Contract",
    "description": "This is an example contract",
    "provider":"https://connectora:8080/",
    "start": "2021-10-22T07:48:37.068Z",
    "end": "2023-10-22T07:48:37.068Z"
}
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T10:24:10.248+0000",
  "modificationDate": "2021-12-16T10:24:10.248+0000",
  "title": "Contract",
  "description": "This is an example contract",
  "start": "2021-12-16T10:24:10.230+0000",
  "end": "2023-10-22T07:48:37.068+0000",
  "consumer": "",
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea"
    },
    "rules": {
      "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea/rules{?page,size}",
      "templated": true
    },
    "offers": {
      "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea/offers{?page,size}",
      "templated": true
    }
  }
}
```

### Add Rule to Contract POST /api/contracts/{id}/rules
Link the created rule to the created contract

Insert the contract id
> 122355bd-f49a-423e-9a3d-15bd55b639ea

Use the rule as the Request body
> [
  "https://localhost:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802"
]

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "rules": [
      {
        "creationDate": "2021-12-16T10:22:02.982+0000",
        "modificationDate": "2021-12-16T10:22:02.982+0000",
        "title": "[\"Example Usage Policy\"^^http://www.w3.org/2001/XMLSchema#string]",
        "description": "",
        "value": "{\n  \"@context\" : {\n    \"ids\" : \"https://w3id.org/idsa/core/\",\n    \"idsc\" : \"https://w3id.org/idsa/code/\"\n  },\n  \"@type\" : \"ids:Permission\",\n  \"@id\" : \"https://w3id.org/idsa/autogen/permission/51f5f7e4-f97f-4f91-bc57-b243714642be\",\n  \"ids:description\" : [ {\n    \"@value\" : \"provide-access\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:title\" : [ {\n    \"@value\" : \"Example Usage Policy\",\n    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n  } ],\n  \"ids:postDuty\" : [ ],\n  \"ids:assignee\" : [ ],\n  \"ids:assigner\" : [ ],\n  \"ids:action\" : [ {\n    \"@id\" : \"https://w3id.org/idsa/code/USE\"\n  } ],\n  \"ids:constraint\" : [ ],\n  \"ids:preDuty\" : [ ]\n}",
        "additional": {},
        "_links": {
          "self": {
            "href": "https://localhost:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802"
          },
          "contracts": {
            "href": "https://localhost:8080/api/rules/fac53177-3e4c-45cf-bdfc-181c3f3e3802/contracts{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea/rules?page=0&size=30"
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

### Artifacts POST /api/artifacts
Create an artifact that contains value "Hello World" with the following Request body
```json
{
    "title": "Artifact",
    "description": "This is an example artifact",
    "value": "Hello World",
    "automatedDownload": true
}
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T10:35:37.850+0000",
  "modificationDate": "2021-12-16T10:35:37.850+0000",
  "remoteId": "genesis",
  "title": "Artifact",
  "description": "This is an example artifact",
  "numAccessed": 0,
  "byteSize": 24,
  "checkSum": 2302133775,
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6"
    },
    "data": {
      "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/data"
    },
    "representations": {
      "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/representations{?page,size}",
      "templated": true
    },
    "agreements": {
      "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/agreements{?page,size}",
      "templated": true
    },
    "subscriptions": {
      "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/subscriptions{?page,size}",
      "templated": true
    }
  }
}
```

### Representations POST /api/representations
Create a representation with the following Request body
```json
{"title": "Example Representation","description": "", "mediaType": "json", "language": "https://w3id.org/idsa/code/EN"}
```

The response body should give code 201 and should have this structure:
```json
{
  "creationDate": "2021-12-16T10:37:10.751+0000",
  "modificationDate": "2021-12-16T10:37:10.751+0000",
  "remoteId": "genesis",
  "title": "Example Representation",
  "description": "",
  "mediaType": "json",
  "language": "https://w3id.org/idsa/code/EN",
  "additional": {},
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d"
    },
    "artifacts": {
      "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/artifacts{?page,size}",
      "templated": true
    },
    "offers": {
      "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/offers{?page,size}",
      "templated": true
    },
    "subscriptions": {
      "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/subscriptions{?page,size}",
      "templated": true
    }
  }
}
```

### Add Artifact to Representation POST /api/representations/{id}/artifacts
Link the created representation to the created artifact

Insert the representation id
> b734b25b-042f-462e-8203-6c8f2ba6852d

Use the artifact as the Request body
> [
  "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6"
]

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "artifacts": [
      {
        "creationDate": "2021-12-16T10:35:37.850+0000",
        "modificationDate": "2021-12-16T10:35:37.850+0000",
        "remoteId": "genesis",
        "title": "Artifact",
        "description": "This is an example artifact",
        "numAccessed": 0,
        "byteSize": 24,
        "checkSum": 2302133775,
        "additional": {},
        "_links": {
          "self": {
            "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6"
          },
          "data": {
            "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/data"
          },
          "representations": {
            "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/representations{?page,size}",
            "templated": true
          },
          "agreements": {
            "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/agreements{?page,size}",
            "templated": true
          },
          "subscriptions": {
            "href": "https://localhost:8080/api/artifacts/d5eb7f14-b99b-4bbf-94e7-4e612a4ccac6/subscriptions{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/artifacts?page=0&size=30"
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

### Add Representation to Offer POST /api/offers/{id}/representations
Link the created representation to the created offer.

Insert the offer id
> 03735877-0111-49a4-b20d-51734c81a991

Use the representation as the Request body
> [
  "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d"
]

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "representations": [
      {
        "creationDate": "2021-12-16T10:37:10.751+0000",
        "modificationDate": "2021-12-16T10:37:10.751+0000",
        "remoteId": "genesis",
        "title": "Example Representation",
        "description": "",
        "mediaType": "json",
        "language": "https://w3id.org/idsa/code/EN",
        "additional": {},
        "_links": {
          "self": {
            "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d"
          },
          "artifacts": {
            "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/artifacts{?page,size}",
            "templated": true
          },
          "offers": {
            "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/offers{?page,size}",
            "templated": true
          },
          "subscriptions": {
            "href": "https://localhost:8080/api/representations/b734b25b-042f-462e-8203-6c8f2ba6852d/subscriptions{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/representations?page=0&size=30"
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

### Add Contract to Offer POST /api/offers/{id}/contracts
Link the created contract to the created offer.

Insert the offer id
> 03735877-0111-49a4-b20d-51734c81a991

Use the contract as the Request body
> [
  "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea"
]

The response body should give code 200 and should have this structure:
```json
{
  "_embedded": {
    "contracts": [
      {
        "creationDate": "2021-12-16T10:24:10.248+0000",
        "modificationDate": "2021-12-16T10:24:10.248+0000",
        "title": "Contract",
        "description": "This is an example contract",
        "start": "2021-12-16T10:24:10.230+0000",
        "end": "2023-10-22T07:48:37.068+0000",
        "consumer": "",
        "additional": {},
        "_links": {
          "self": {
            "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea"
          },
          "rules": {
            "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea/rules{?page,size}",
            "templated": true
          },
          "offers": {
            "href": "https://localhost:8080/api/contracts/122355bd-f49a-423e-9a3d-15bd55b639ea/offers{?page,size}",
            "templated": true
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://localhost:8080/api/offers/03735877-0111-49a4-b20d-51734c81a991/contracts?page=0&size=30"
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


## Registering resources at connector B
TODO

## Register connector A and B at the MetaData Broker
### Connector A

At the Swagger UI of the Connector A insert the following recipient url for message POST /api/ids/connector/update:
> https://broker-localhost_broker-reverseproxy_1/infrastructure

The server response should give code 200.

### Connector B

At the Swagger UI of the Connector B insert the following recipient url for message POST /api/ids/connector/update:
> https://broker-localhost_broker-reverseproxy_1/infrastructure

The server response should give code 200.

## Checking successful registration
Query the MetaData Broker for available data in the Testbed
using messages POST /api/ids/description via one of the connectors' Swagger UIs.

Insert the recipient url
> https://broker-localhost_broker-reverseproxy_1/infrastructure

Insert the element id of the requested resource
> https://localhost/connectors/

The response body should give code 200 and should include both connectors in the list of connectors.
