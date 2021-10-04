# DAPS

To obtain your own Fraunhofer AISEC certificate and use it in the Testbed, request one in: https://industrialdataspace.jiveon.com/docs/DOC-2002 (Requires Jive access).

In this example, the certificate from the Testbed CA is going to be featured (TestbedCert).

## Installation steps
From the IDS-testbed directory,
> cd Testbed/OmejdnDAPS
>
> unzip omejdnDAPS.zip
>
> cd omejdnDAPS

## Interoperability configuration steps (WITH THE TESTBED CERTIFICATE)
1. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
- The client_id value can be extracted from the .cert file: 87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:10:3C:88:0E:53:02
- **add** under attributes: certfile: TestbedCert.cert

2. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/omejdn.yml
- host: idsc:IDS_CONNECTORS_ALL
- audience: idsc:IDS_CONNECTORS_ALL

3. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/keys

- Copy the public key (.cert) into this directory.

## Interoperability configuration steps (WITH A FRAUNHOFER AISEC CERTIFICATE)
1. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/clients.yml
- client_id: Paste the aki/ski extensions from the certificate used in the component to interact with the DAPS, further detailed in the example
- **add** under "attributes": certfile: {your_cert}.cert

2. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/config/omejdn.yml
- host: idsc:IDS_CONNECTORS_ALL
- audience: idsc:IDS_CONNECTORS_ALL

3. /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/keys
- Copy your own public key (.cert) into this directory.


## Testing interoperability

### With the script within OmejdnDAPS

* /IDS-testbed/Testbed/OmejdnDAPS/omedjdnDAPS/scripts
1. Drop your certificate's private key here {file}.key
2. In create_test_token.rb, edit CLIENTID to have the same information as {file} in the {file}.key in step 1
3. Edit the 'iss' and 'sub' in the second half of the script to have the same information added to client_id in config/clients.yml.
4. Edit the 'aud': idsc:IDS_CONNECTORS_ALL

Notice: The content in steps 3 and 4 must be inside single quotes.

By making config/client_id, 'iss' and 'sub' in the script equals to the certificate's aki/ski extensions, we have already configured the DAPS to give DATs to the component.


### Testing Interoperability (WITH THE TESTBED CERTIFICATE)

* /IDS-testbed/Testbed/OmejdnDAPS/omejdnDAPS/scripts

Copy the TestbedCert.key into this directory.

Go to scripts/create_test_token.rb and replace the CLIENTID value with the {file} value {file}.key. 
In our case, from TestbedCert.key we get TestbedCert
> CLIENTID = 'TestbedCert'

Make sure you modify "keys/#{CLIENTID}.key" with "#{CLIENTID}.key" to avoid issues.


It is important to note that 'iss' and 'sub' must have the same value. Also, the 'iss' and 'sub' values should be the same as client_id in OmejdnDAPS/omejdnDAPS/config/clients.yml
> 'iss' => '87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:10:3C:88:0E:53:02'
> 
> 'sub' => '87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:10:3C:88:0E:53:02'

The 'aud' value must be changed to 'idsc:IDS_CONNECTORS_ALL'. That is what the implementation of IDS connectors use in the 'aud' field.
> 'aud' => 'idsc:IDS_CONNECTORS_ALL'

#### Launch the DAPS:
Build an image with the following command. Feel free to replace "daps" with another name. 
> docker build . -t daps

Build the container with the image created.
> docker run -d --name=omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys daps

Ensure the DAPS server is running: http://localhost:4567

#### Run the script:
In the terminal, go to /IDS-testbed/Testbed/OmejdnDAPS/omedjdnDAPS/scripts and run the script:
> ruby create_test_token.rb

A token is received.

#### Requesting an access token
Use the JWT bearer token just received to request a DAT from the DAPS. Insert the token received in {INSERT_TOKEN_HERE} and run the following command:
```
    $ curl localhost:4567/token --data "grant_type=client_credentials
                                        &client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
                                        &client_assertion={INSERT_TOKEN_HERE}
                                        &scope=ids_connector security_level"
```
> curl localhost:4567/token --data "grant_type=client_credentials&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_assertion={INSERT_TOKEN_HERE}&scope=ids_connector security_level"

If everything is setup correctly, a Dynamic Attribute Token (DAT) will be received and therefore, interoperability achieved.
