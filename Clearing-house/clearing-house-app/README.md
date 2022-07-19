# Clearing House App

The `Clearing House App` is a REST API written in [Rust](https://www.rust-lang.org) that implements the business logic of the Clearing House. Currently, it only implements the [`Logging Service`](https://github.com/International-Data-Spaces-Association/IDS-RAM_4_0/blob/main/documentation/3_Layers_of_the_Reference_Architecture_Model/3_5_System_Layer/3_5_5_Clearing_House.md), which depends on two micro services:

1. Document API
2. Keyring API

The Document API is responsible for storing the data, while the Keyring API provides cryptographic support for encryption and decryption of the stored data.

## Requirements
- [Rust](https://www.rust-lang.org)
- [OpenSSL](https://www.openssl.org)
- [MongoDB](https://www.mongodb.com)
- ([Docker](https://www.docker.com))

## Configuration

### Logging Service
The `Logging Service` is configured using the configuration file [`Rocket.toml`](logging-service/Rocket.toml), which must specify a set of configuration options, such as the correct URLs of the database and other service apis:
- `daps_api_url`: Specifies the URL of the DAPS Service. Required to validate DAPS token
- `keyring_api_url`: Specifies the URL of the Keyring API
- `document_api_url`: Specifies the URL of the Document API
- `database_url`: Specifies the URL of the database to store process information. Currently only mongodb is supported so URL is supposed to be `mongodb://<host>:<port>`
- `clear_db`: `true` or `false` indicates if the database should be cleared when starting the Service API or not. If `true` a restart will wipe the database! Starting the Service API on a clean database will initialize the database.
- `signing_key`: Location of the private key (DER format) used for signing the Receipts. Clearing House uses PS512 algorithm for signing.

More information on general configuration options in a `Rocket.toml` file can be found [here](https://rocket.rs/v0.5-rc/guide/configuration/#rockettoml).

#### Logging
The `Logging Service` also needs the following environment variables set:
- `API_LOG_LEVEL`: Allowed log levels are: `Off`, `Error`, `Warn`, `Info`, `Debug`, `Trace`

#### Signing Key
The `Logging Service` sends a signed receipt as response to a logging request. The key can be created using openssl:

`openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -outform der -out private_key.der`

Please note that the Clearing House requires the key to be in DER format. It must be available to the `Logging Service` under the path configured in `Rocket.toml`, e.g. `/server/keys/private_key.der`.

#### Example Configuration (docker-compose)
```
logging-service:
    container_name: "logging-service"
    depends_on:
        - document-api
        - keyring-api
        - logging-service-mongo
    environment:
        # Allowed levels: Off, Error, Warn, Info, Debug, Trace
        - API_LOG_LEVEL=Debug
    ports:
        - "8000:8000"
    volumes:
        - ./data/Rocket.toml:/server/Rocket.toml
        - ./data/keys:/server/keys
        - ./data/certs:/server/certs
```

### Document API
The `Document API` is responsible for storing the data and performs basic encryption and decryption for which it depends on the Keyring API. It is configured using the configuration file [`Rocket.toml`](document-api/Rocket.toml), which must specify a set of configuration options, such as the correct URLs of the database and other service apis:
- `daps_api_url`: Specifies the URL of the DAPS Service. Required to validate DAPS token
- `keyring_api_url`: Specifies the URL of the Keyring API
- `database_url`: Specifies the URL of the database to store the encrypted documents. Currently only mongodb is supported so URL is supposed to be `mongodb://<host>:<port>`
- `clear_db`: `true` or `false` indicates if the database should be cleared when starting the Service API or not. If `true` a restart will wipe the database! Starting the Service API on a clean database will initialize the database.

#### Logging
The `Document API` also needs the following environment variables set:
- `API_LOG_LEVEL`: Allowed log levels are: `Off`, `Error`, `Warn`, `Info`, `Debug`, `Trace`

#### Example Configuration (docker-compose)
```
document-api:
    container_name: "document-api"
    depends_on:
        - keyring-api
        - document-mongo
    environment:
        # Allowed levels: Off, Error, Warn, Info, Debug, Trace
        - API_LOG_LEVEL=Info
    ports:
        - "8001:8001"
    volumes:
        - ./data/document-api/Rocket.toml:/server/Rocket.toml
        - ./data/certs:/server/certs
```

### Keyring API
The `Keyring API` is responsible for creating keys and the actual encryption and decryption of stored data. It is configured using the configuration file [`Rocket.toml`](keyring-api/Rocket.toml), which must specify a set of configuration options, such as the correct URLs of the database and other service apis:
- `daps_api_url`: Specifies the URL of the DAPS Service. Required to validate DAPS token
- `database_url`: Specifies the URL of the database to store document types and the master key. Currently only mongodb is supported so URL is supposed to be `mongodb://<host>:<port>`
- `clear_db`: `true` or `false` indicates if the database should be cleared when starting the Service API or not. If `true` a restart will wipe the database! Starting the Service API on a clean database will initialize the database.

#### Logging
The `Keyring API` also needs the following environment variables set:
- `API_LOG_LEVEL`: Allowed log levels are: `Off`, `Error`, `Warn`, `Info`, `Debug`, `Trace`

The Keyring API requires that its database contains the acceptable document types. Currently only the IDS_MESSAGE type is supported and needs to be present in the database for the Keyring API to function properly. The database will be populated with an initial document type that needs to be located in `init_db/default_doc_type.json`.

#### Example Configuration (docker-compose)
```
keyring-api:
    container_name: "keyring-api"
    depends_on:
        - keyring-mongo
    environment:
        # Allowed levels: Off, Error, Warn, Info, Debug, Trace
        - API_LOG_LEVEL=Info
    ports:
        - "8002:8002"
    volumes:
        - ./data/keyring-api/init_db:/server/init_db
        - ./data/keyring-api/Rocket.toml:/server/Rocket.toml
        - ./data/certs:/server/certs
```

### DAPS
The `Logging Service` and the micro services need to be able to validate the certificate used by the DAPS. If the DAPS uses a self-signed certificate the certificate needs to be added in two places for each service:
1. `/server/certs`: The service will load certificates in this folder in the container and use them for validation. The certificate needs to be in DER format.
2. `/usr/local/share/ca-certificates`: The service relies on openssl for parts of the validation and openssl will not trust a self-signed certificate unless it was added in this folder and `update-ca-certificates` was called in the docker container. Once this is done the container might need to be restarted.

If you are using [these dockerfiles](../docker/) and use `daps.aisec.fraunhofer.de` as the DAPS, you only need to follow Step 1. The certificate needed for Step 1 can be found [here](certs).

### Mongo DB
Each service requires a MongoDB for storing data. One easy way to ensure this is to configure a docker container for each service like this:

```
logging-service-mongodb:
    container_name: "logging-service-mongodb"
    image: mongo:latest
    environment: 
        MONGO_INITDB_DATABASE: process
    volumes:
        - ./data/mongo/logging-service:/data/db
```

The services are configured to store data in different `databases`:

| Service | `database` |
|-----|------|
|`Logging Service`| `process`|
|`Document API`| `document`|
|`Keyring API`| `keyring`|

Please ensure that the mongodb instance(s) you are using provide(s) the required `database` for the respective service(s). With docker this can be achieved using the `MONGO_INITDB_DATABASE` environment variable. Please note that you do not need mount a volume for mongodb to store the data persistently. However, if you do not mount a volume docker will store the data inside the container and the lifespan of the data is bound to the container. For more information on this and how to properly configure a mongodb container, please refer to the official mongodb docker [documentation](https://hub.docker.com/_/mongo).

## Building from Source
The Logging service and micro services are written in [Rust](https://www.rust-lang.org) and can be build using

```
cargo build --release
```

The build requires OpenSSL to be installed.
