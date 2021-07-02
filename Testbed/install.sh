#!/bin/bash
# Software dependencies
sudo apt-get install maven
sudo apt install openjdk-11-jdk
sudo apt install curl
sudo apt isntall ruby
sudo gem install jwt

## DataspaceConnector

# Unzip the folder
cd DataspaceConnector
unzip DataspaceConnector-main.zip
cd ..
# Copy the certificates to the correct folders
cp certs/dsc/testidsa1.p12 DataspaceConnector/DataspaceConnector-main/src/main/resources/conf

sed -i '12s/idsc:TEST_DEPLOYMENT/idsc:PRODUCTIVE_DEPLOYMENT/' DataspaceConnector/src/main/resources/conf/config.json
sed -i '60s/keystore-localhost.p12/testidsa1.p12/' DataspaceConnector/src/main/resources/conf/config.json

cd DataspaceConnector/DataspaceConnector-main
mvn clean package
cd target
gnome-terminal -- java -jar dataspaceconnector-5.1.2.jar
cd ../../..

## Broker
sudo mkdir -p /etc/idscert/localhost
sudo cp -n certs/broker/server.key /etc/idscert/localhost
sudo cp -n certs/broker/server.crt /etc/idscert/localhost

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
cp certs/daps/testidsa1.key OmejdnDAPS/omejdn-daps-master/scripts
cd OmejdnDAPS/omejdn-daps-master

docker build . -t daps

if [ ! "$(docker ps -q -f name=omejdn)" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=omejdn)" ]; then
        # cleanup
        docker rm omejdn
    fi
    # run your container
    docker run -d --name omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys daps
fi
