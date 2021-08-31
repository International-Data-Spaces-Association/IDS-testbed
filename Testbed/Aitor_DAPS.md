# DAPS

## Quick information regarding the current state of the component
The DAPS server requires the public keys (.cert) from those components that wish to obtain a DAT from it. This .cert file should be placed in the "keys" directory within the DAPS directory.

For the time being, these certificates will be the ones provided by Fraunhoder AISEC. For the time being, you can use a certificate located in the "certs" folder in the "Testbed" folder. If you wanna get your own certificate and use it in the Testbed, request one in: https://industrialdataspace.jiveon.com/docs/DOC-2002 (Requires Jive access).

Once the CA has the proper aki/ski extensions, the certificates used in the Testbed will be provided by it making the Testbed a closed environment.

## Installation steps
> cd OmejdnDAPS
>
> unzip omejdnDAPS.zip
>
> cd omejdnDAPS

## Configuration steps (WITH A FRAUNHOFER AISEC CERTIFICATE)
* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
1. client_id: testClient1 (Change this into the aki/ski extensions from the certificate used in the component to interact with the DAPS)
2. **add** under "attributes": certificate: {your_cert}.cert

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/omejdn.yml
1. host: idsc:IDS_CONNECTORS_ALL
2. audience: idsc:IDS_CONNECTORS_ALL

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/keys
1. Drop the .cert certificate from the "cert" directory in /IDS-testbed/Testbed/daps here.

OR

2. Drop the .cert certificate you have obtained directly from Fraunhofer AISEC.

OR

3. Drop the .cert certificate you obtained from the CA here. (This will be the only/recommended option in the future)




