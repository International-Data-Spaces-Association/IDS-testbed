# DAPS

## Quick information regarding the current state of the component
The DAPS server requires the public keys (.cert) from those components that wish to obtain a DAT from it. This .cert file should be placed in the "keys" directory within the DAPS directory.

For the time being, these certificates will be the ones provided by Fraunhoder AISEC. Feel free to use the certificate located in the "certs" folder in the "Testbed" folder. If you want to get your own certificate and use it in the Testbed, request one in: https://industrialdataspace.jiveon.com/docs/DOC-2002 (Requires Jive access).

Once the CA has the proper aki/ski extensions, the certificates used in the Testbed will be provided by the Testbed CA.

## Installation steps
From the IDS-testbed directory,
> cd Testbed/OmejdnDAPS
>
> unzip omejdnDAPS.zip
>
> cd omejdnDAPS

## Interoperability configuration steps (WITH A FRAUNHOFER AISEC CERTIFICATE)
* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
1. client_id: testClient1 (Change this into the aki/ski extensions from the certificate used in the component to interact with the DAPS, explained in the end)
2. **add** under "attributes": certfile: {your_cert}.cert

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/omejdn.yml
1. host: idsc:IDS_CONNECTORS_ALL
2. audience: idsc:IDS_CONNECTORS_ALL

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/keys
1. Copy the .cert certificate from the "cert" directory in /IDS-testbed/Testbed/daps here.

OR

2. Copy the .cert certificate you have obtained directly from Fraunhofer AISEC.

OR

3. Copy the .cert certificate you obtained from the CA here. (This will be the only/recommended option in the future)

## Testing interoperability

### With the script within OmejdnDAPS

* /IDS-testbed/Testbed/OmejdnDAPS/omedjdnDAPS/scripts
1. Drop your certificate's private key here {file}.key
2. Edit CLIENTID to have the same information as {file} in the {file}.key in step 1
3. Edit the 'iss' and 'sub' in the second half of the script to have the same information added to client_id in /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml.
4. Edit the 'aud': idsc:IDS_CONNECTORS_ALL

Notice: The content in steps 3 and 4 must be inside single quotes.

By making config/client_id, 'iss' and 'sub' in the script equals to the certificate's aki/ski extensions, we have already configured the DAPS to give DATs to the certificate.

## The step by step process using the certificate provided

### Interoperability configuration steps (WITH THE INCLUDED CERTFICATE)
* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
1. client_id: 4A:C3:0A:F2:CD:17:34:1A:FB:D7:28:FF:8F:B9:F7:B1:17:28:1D:71:keyid:CB:8C:C7:B6:85:79:A8:23:A6:CB:15:AB:17:50:2F:E6:65:43:5D:E8
2. **add** certfile: testidsa1.cert

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/omejdn.yml
host: idsc:IDS_CONNECTORS_ALL
audience: idsc:IDS_CONNECTORS_ALL

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/keys
Copy the .cert from IDS-testbed/Testbed/certs/daps/testidsa1.cert into this directory.

In the command line, go to the IDS-testbed directory. Then:
> cp Testbed/certs/daps/testidsa1.cert Testbed/OmejdnDAPS/omejdnDAPS/keys

### Testing Interoperability (WITH THE INCLUDED CERTIFICATE)

* /IDS-testbed-Testbed-OmejdnDAPS/omejdnDAPS/scripts
Copy the .key from IDS-testbed/Testbed/certs/daps/testidsa1.key into this directory.

In the command line, go to the IDS-testbed directory. Then copy the private key into the script directory:
> cp Testbed/certs/daps/testidsa1.key Testbed/OmejdnDAPS/omejdnDAPS/scripts

Replace the CLIENDID value with the {file} value {file}.key. In our case, from testidsa1.key we get testidsa1
> CLIENDID = 'testidsa1'

It is important to note that 'iss' and 'sub' must have the same value. Also, the 'iss' and 'sub' values should be the same as client_id in /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
> 'iss' => '4A:C3:0A:F2:CD:17:34:1A:FB:D7:28:FF:8F:B9:F7:B1:17:28:1D:71:keyid:CB:8C:C7:B6:85:79:A8:23:A6:CB:15:AB:17:50:2F:E6:65:43:5D:E8'
> 'sub' => '4A:C3:0A:F2:CD:17:34:1A:FB:D7:28:FF:8F:B9:F7:B1:17:28:1D:71:keyid:CB:8C:C7:B6:85:79:A8:23:A6:CB:15:AB:17:50:2F:E6:65:43:5D:E8'
The 'aud' value should be changed to 'idsc:IDS_CONNECTORS_ALL'. That is what the implementation of IDS connectors use in the 'aud' field.
> 'aud' => 'idsc:IDS_CONNECTORS_ALL'
