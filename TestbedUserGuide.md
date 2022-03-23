# Testbed User Guide

This user guide is meant to explain to each testbed user what they should do with the testbed in order to assess the compatibility of their own developed component.

## 1. Download and Install the Testbed
Follow the instructions in the [installation and configuration guide](./README.md) to install and configure the testbed as required. Utilize the preconfigured setup described as basis for the this user guide.

## 2. Integrating your Connector into the Ecosystem
### 2.1. Generate a Certificate for your Connector
Generate a private-public key pair for your connector.
Issue a certificate for the public key in this key pair signed by the private key of the utilized testbed CA:
```bash
./pki.py cert sign --key-file [path to public key file] --subCA [Sub CA name] --common-name [common name] --client
```
For the preconfigured setup, the name of the subCA is "ReferenceTestbedSubCA" and the respective files are found in [CertificateAuthority/data/subca/](./CertificateAuthority/data/subca/).

Ensure that your connector always utilizes this IDS certificate to prove their identity with respect to the other components.

### 2.2. Configure your Connector
* Configure your connector to use the common Root CA (cert). For the preconfigured setup, the file to be utilized is [CertificateAuthority/data/ca/ReferenceTestbedCA.crt](./CertificateAuthority/data/ca/ReferenceTestbedCA.crt)
* Configure your connector to use the DAPS available under http://localhost:4567 (endpoints: /token, /.well-known/jwks.json)
* Provide a self-description for your connector

## 3. Interacting with the DAPS
### 3.1. Register your connector at the DAPS
Register your connector following  
a) the instructions provided here:
https://github.com/International-Data-Spaces-Association/omejdn-daps#registering-connectors  
or b) the manual steps described below:
1. Convert your connector certificate {common name}.crt from step 2.1 into the format required by the DAPS:
```
## .crt + .key -> .p12
openssl pkcs12 -export -out {common name}.p12 -inkey {common name}.key -in {common name}.crt -passout pass:password
## .p12 -> .cert
openssl pkcs12 -in {common name}.p12 -out {common name}.cert -nokeys -nodes -passin pass:password
```
2. Add the certificate {common name}.cert to the OmejdnDAPS/keys directory
3. Add your client information identified by the unique identifier (client_id) in the OmejdnDAPS/config/clients.yml file following the given examples in this file.
The provided script [OmejdnDAPS/keys/extensions.sh](./OmejdnDAPS/keys/extensions.sh) can extract the unique identifier from the certificate to help the users who are not familiar with AKI/SKI extensions.

### 3.2. Request your DAT
* Use your connector to request a DAT from the DAPS
* Validate that you received a valid DAT corresponding to the specification:
https://github.com/International-Data-Spaces-Association/IDS-G/blob/main/Components/IdentityProvider/DAPS/README.md#dynamic-attribute-token-dat

## 4. Interacting with Connectors
### 4.1. Request Self-descriptions from Available Connectors
***Connector A***  
Connector A is available at the following URL: https://localhost:8080

Request the Self-Description from Connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - still work in progress
  * IDS-REST: not yet supported by connector A

Validate that you receive the following self-description:
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:BaseConnector",
  "@id" : "https://connector_A",
  "ids:description" : [ {
    "@value" : "IDS Connector A with static example resources",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:version" : "6.2.0",
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
    "@id" : "https://localhost:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f",
    "ids:offeredResource" : [ ],
    "ids:requestedResource" : [ ]
  }],
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
```

***Connector B***  
Connector B is available at the following URL: https://localhost:8081

Request the Self-Description from Connector B using those of the following protocols that you support:
  * Multipart: currently supported by connector B
  * IDSCP2: currently supported by connector B - still work in progress
  * IDS-REST: not yet supported by connector B

Validate that you receive the following self-description:
```json
{
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:BaseConnector",
  "@id" : "https://connector_B",
  "ids:version" : "6.2.0",
  "ids:description" : [ {
    "@value" : "IDS Connector B with static example resources",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:resourceCatalog" : [ ],
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
  },
  "ids:title" : [ {
    "@value" : "Dataspace Connector",
    "@type" : "http://www.w3.org/2001/XMLSchema#string"
  } ],
  "ids:hasDefaultEndpoint" : {
    "@type" : "ids:ConnectorEndpoint",
    "@id" : "https://w3id.org/idsa/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf",
    "ids:accessURL" : {
      "@id" : "https://connectorb:8081/api/ids/data"
    },
    "ids:endpointDocumentation" : [ ],
    "ids:endpointInformation" : [ ]
  },
  "ids:hasEndpoint" : [ ]
}
```

### 4.2. Request Data from Available Connectors
***Connector A***  
Connector A offers an exemplary data artifact with weather warnings from the DWD. Obtain the resource catalog @id from the Self-Description requested in 4.1
> https://connectora:8080/api/catalogs/2cd59c94-54e4-4979-9842-36ee45dd354f

Request the data sets from connector A using those of the following protocols you support:
  * Multipart: currently supported by connector A
  * IDSCP2: currently supported by connector A - still work in progress
  * IDS-REST: not yet supported by connector A

Validate that you receive as data the following URL which can be used to obtain the corresponding DWD weather data:
https://maps.dwd.de/geoserver/dwd/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=dwd%3AAutowarn_Analyse&maxFeatures=50&outputFormat=application%2Fjson

***Connector B***
Connector B is currently not offering any data sets but only helping with validating the correct testbed setup as described in the [Guide for Preparing and Validating the Preconfigured Setup](./PreparingPreconfiguredSetup.md).

## 5. Interacting with the MetaData Broker
## 5.1. Query the Self-description of the MetaData Broker
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

### 5.2. Query the MetaData Broker for Available Connectors in the Testbed
The MetaData Broker can be reached at https://localhost[:443] and is aware of the self-descriptions of connector A and B.

Query the MetaData Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker

Validate that you received the following list of connectors:
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

### 5.3. Register your connector at the IDS MetaDataBroker with an exemplary data set
Register your connector at the MetaData Broker using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker

Query the MetaData Broker for all available datasets using those of the following protocols you support:
  * Multipart: currently supported by the MetaData Broker
  * IDSCP2: not yet supported by the MetaData Broker
  * IDS-REST: not yet supported by the MetaData Broker

Validate that the received list of connectors represents the list retreived in 5.2, but extended for your own provided information.

## In Addition: Execute Automated Test Suite

Please execute the automated (interoperability) test suite provided at the Testsuite folder by [following the installation and execution instructions](./Testsuite/README.md).
