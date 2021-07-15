# Certificate Authortiy (CA)

## Link to the current specification
Official

Internal Preparation
latest version in the slideset on the certificate policy presented in the working group meetings, can be found attached to the WG Certification meetings: https://industrialdataspace.jiveon.com/docs/DOC-3611

## Description

This software is intended to be used as a mock CA for test environments. It does not offer any security guarantees and must not be used in productive environments.

## Scope

This software simulates a PKI with root CAs, intermediate CAs and device certificates. Only PKIs with exactly one level of intermediate CAs are possible.

The software supports secp256r1, secp384r1, secp521r1 as well as RSA with different key sizes. It also supports sha256, sha384 and sha512 as signing hashes.

## Dependencies

The software requires Python 3 and the Python OpenSSL library to be present. On Ubuntu this can be achieved by installing the package `python3-openssl`.

## Usage

### Initialization

Before any other operation is possible the necessary data directory must be initialized. This can be done with the following command:
```bash
./pki.py init
```
**Caution:** This operation removes all created CAs, sub CAs and certificates.

### Creation of a Root CA

A root CA can then be created with the following command:
```bash
./pki.py ca create --common-name "Test CA"
```
A list of available parameters with their defaults can be obtained by:
```bash
./pki.py ca create -h
```
All root CAs created can be listed with the following command:
```bash
./pki.py ca list
```

### Creation of a Sub CA

A sub CA can then be created with the following command:
```bash
./pki.py subca create --CA [CA name] --common-name "Test CA"
```
The CA used for signing the sub CA is a required parameter.

A list of available parameters with their defaults can be obtained by:
```bash
./pki.py subca create -h
```
All sub CAs created can be listed with the following command:
```bash
./pki.py subca list
```

### Creation of a Device Certificate

A device certificate can then be created with the following command:
```bash
./pki.py cert create --subCA [Sub CA name] --common-name "example.com"
```
The Sub CA used for signing the certificate is a required parameter.

A list of available parameters with their defaults can be obtained by:
```bash
./pki.py cert create -h
```
All device certificates created can be listed with the following command:
```bash
./pki.py cert list
```
