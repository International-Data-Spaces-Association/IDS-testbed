#!/bin/bash
##### Software dependencies
#sudo dpkg --configure -a
sudo apt-get install maven
sudo apt-get install docker
sudo apt-get install docker-compose
sudo apt install openjdk-11-jdk
sudo apt install curl
sudo apt install ruby
sudo gem install jwt
sudo apt install python3-openssl

##### Installation and configuration of components
docker network create testbed

#### Certification Authority

### Setup the component
cd CertificationAuthority
unzip CertificationAuthority.zip
cd CertificationAuthority

### Get started
chmod +x pki.py
./pki.py init
## Uncomment for creating a new CA, SubCA, and certs
#./pki.py ca create --common-name "Testbed CA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"
#./pki.py subca create --CA "Testbed CA" --common-name "Testbed SubCA" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS"
#./pki.py cert create --subCA "Testbed SubCA" --common-name "TestbedCert1" --algo "rsa" --bits "2048" --country-name "ES" --organization-name "SQS" --client --server
## Certificate extension formatting
#cd data/cert
#openssl pkcs12 -export -out TestbedCert1.p12 -inkey TestbedCert1.key -in TestbedCert1.crt -passout pass:password
#openssl pkcs12 -in TestbedCert1.p12 -out TestbedCert1.cert -nokeys -nodes -passin pass:password
## Transfer the certificate to the component
#cp TestbedCert1.p12 ../../../../DataspaceConnector/DataspaceConnector/DataspaceConnector-main/src/main/resources/conf
#cp TestbedCert1.cert ../../../../OmejdnDAPS/OmejdnDAPS/keys


#### Dataspace Connector
# Uncomment the line below if the CA above was used to create the CA/SubCA/Cert
#cd ../../../../DataspaceConnector

### Setup the component
cd ../../DataspaceConnector
unzip DataspaceConnector.zip
cd DataspaceConnector

### Get started
sed -i '59s,localhost,omejdn,' src/main/resources/application.properties
sed -i '60s,localhost,omejdn,' src/main/resources/application.properties
sed -i '12s,TEST_DEPLOYMENT,PRODUCTIVE_DEPLOYMENT,' src/main/resources/conf/config.json
sed -i '60s,keystore-localhost.p12,TestbedCert.p12,' src/main/resources/conf/config.json
docker build -t dsc .

### Launch the component
if [ ! "$(docker ps -q -f name=dsccontainer)" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=dsccontainer)" ]; then
        # cleanup
        docker rm dsccontainer
    fi
    # run your container
    docker run --publish 8080:8080 --detach --name dsccontainer --network=testbed dsc
fi


#### Omejdn DAPS

### Setup the component
cd ../../OmejdnDAPS
unzip OmejdnDAPS.zip
cd OmejdnDAPS

sed -i '2s,testClient,87:B9:0A:10:F3:82:97:AF:DA:1E:05:47:5F:8B:AD:46:23:8B:47:6F:keyid:54:07:82:AE:07:B1:BA:9A:00:67:10:95:C8:EC:10:3C:88:0E:53:02,' config/clients.yml
sed -i '8s,testClient,TestbedCert.cert,' config/clients.yml

sed -i '2s,http://localhost:4567,idsc:IDS_CONNECTORS_ALL,' config/omejdn.yml
sed -i '8s,TestServer,idsc:IDS_CONNECTORS_ALL,' config/omejdn.yml

### Get started
docker build -t daps .

### Launch the component
if [ ! "$(docker ps -q -f name=omejdn)" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=omejdn)" ]; then
        # cleanup
        docker rm omejdn
    fi
    # run your container
    docker run -d --name omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys --network=testbed daps
fi

##### Check the components are running
echo "Checking Certification Authority availability..."
sleep 6
echo "Checking Dataspace Connector availability..."
sleep 6
echo "Checking OmejdnDAPS availability..."
sleep 6


#### Certification Authority
cd ../../CertificationAuthority/CertificationAuthority/data &>/dev/null && echo "> SUCCESS - The Certification Authority is working" || echo "> ERROR - The Certification Authority is not working"
#### Dataspace Connector
curl -k -s "https://localhost:8080" &>/dev/null && echo "> SUCCESS - The Dataspace Connector is working" || echo "> ERROR - The Dataspace Connector is not working"
#### Omejdn DAPS
curl -k -s "http://localhost:4567" &>/dev/null && echo "> SUCCESS - The Omejdn DAPS is working properly" || echo "> ERROR - The Omejdn DAPS is not working"
#### Metadata Broker
curl -k -s "https://localhost" &>/dev/null && echo "> SUCCESS - The Metadata Broker is working properly" || echo "> ERROR - The Metadata Broker is not working"


