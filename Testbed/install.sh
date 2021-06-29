#!/bin/bash
## SCRIPT IS BEING UPDATED TO MAKE THE INSTALLER WORK SMOOTHLY

## DataspaceConnector
# Unzip the folder
cd DataspaceConnector
unzip DataspaceConnector-main.zip
cd ..
# Copy the certificates to the correct folders
cp certs/dsc/testidsa1.p12 DataspaceConnector/DataspaceConnector-main/src/main/resources/conf

sed -i '12s/idsc:PRODUCTIVE_DEPLOYMENT/idsc:TEST_DEPLOYMENT/' DataspaceConnector/src/main/resources/conf/config.json
sed -i '60s/testids.p12/testaitor.p12/' DataspaceConnector/src/main/resources/conf/config.json

cd DataspaceConnector-main
mvn clean package
cd target
gnome-terminal -- java -jar dataspaceconnector-5.1.2.jar
cd ../../..

## Broker
sudo mkdir -p /etc/idscert/localhost
sudo cp -i certs/broker/server.key /etc/idscert/localhost
sudo cp -i certs/broker/server.crt /etc/idscert/localhost

cd MetadataBroker
unzip metadata-broker-open-core.zip
cd metadata-broker-open-core-master/docker/composefiles/broker-localhost

docker-compose pull

## Uncomment the following line if issue appears regarding port 80 already in use
sed -i "10s/80:80/81:80/" docker-compose.yml

gnome-terminal -- docker-compose up

cd ../../../../..
## DAPS
cd OmejdnDAPS
unzip omejdn-daps.zip
cd ..
cp certs/daps/testidsa1.cert OmejdnDAPS/omejdn-daps-master/keys
cd OmejdnDAPS/omejdn-daps-master
docker build . -t daps
docker run -d --name=omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys daps
