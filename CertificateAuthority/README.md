# Certificate Authority (CA)

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

## Installation

```bash
$ git clone https://github.com/International-Data-Spaces-Association/IDS-testbed.git
$ cd CertificateAuthority
$ sudo apt install python3-openssl
```

## Usage

### Initialization

Before any other operation is possible the necessary data directory must be initialized. This can be done with the following command:
```bash
python3 pki.py  init
```
**Caution:** This operation removes all created CAs, sub CAs and certificates.

### Creation of a Root CA

A root CA can then be created with the following command:
```bash
python3 pki.py  ca create --common-name [CA name] --organization-name [O] --country-name [C] --unit-name [OU] --hash [Algorithm for signing] 
```
It could look something like this
```bash
python3 pki.py ca create --common-name ReferenceTestbedCA --organization-name SQS --country-name ES --unit-name TestLab --hash sha512 
```
A list of available parameters with their defaults can be obtained by:
```bash
python3 pki.py ca create -h
```
All root CAs created can be listed with the following command:
```bash
python3 pki.py ca list
```

### Creation of a Sub CA

A sub CA can then be created with the following command:
```bash
python3 pki.py subca create --CA [CA name] --common-name [Sub CA name] --organization-name [O] --country-name [C] --unit-name [OU] --hash [Algorithm for signing]
```
It could look something like this
```bash
python3 pki.py subca create --CA "ReferenceTestbedCA" --common-name "ReferenceTestbedSubCA" --organization-name SQS --country-name ES --unit-name TestLab --hash sha384
```
The CA used for signing the sub CA is a required parameter.

A list of available parameters with their defaults can be obtained by:
```bash
python3 pki.py subca create -h
```
All sub CAs created can be listed with the following command:
```bash
python3 pki.py subca list
```

### Creation of a Device Certificate

**Creation of key pair and certificate in one step**  
A device private key with the respective certificate can be created with the following command:
```bash
python3 pki.py cert create --subCA [Sub CA name] --common-name [Cert name] --algo [Key algorithm] --bits [Bits of Key] --hash [Algorithm for signing] --client --server --san-name [DNS Name] --san-ip [IP Address]
```

Additionally, it can be included country name, organization name and unit name information.

It could look something like this
```bash
python3 pki.py cert create --subCA ReferenceTestbedSubCA --common-name Example --algo rsa --bits 2048 --hash sha256 --country-name ES --organization-name SQS --unit-name TestLab --server --client --san-name ExampleDNS --san-ip 127.0.0.1
```
The Sub CA used for signing the certificate is a required parameter. The key algorithm `rsa`, bits of key `2048`, algorithm for signing `sha256` and Subject Alternative Name with DNS Name and IP Address are also required for correct interoperability between IDS-testbed components.

The created key pair is located at the folder `CertificateAuthority/data/cert`

**Creation of a certificate for an existing key pair**  
If a private-public key pair is already available on the device, the public key can be signed to gain a device certificate with the following command:
```bash
python3 pki.py cert sign --key-file [path to public key file] --subCA [Sub CA name] --common-name "Example" --client --server
```
The path to the (public) key file and the Sub CA used for signing the certificate are required parameters. The public key file must be provided in PEM format.

A list of available parameters with their defaults can be obtained by:
```bash
python3 pki.py cert create -h
```
All device certificates created can be listed with the following command:
```bash
python3 pki.py cert list
```
