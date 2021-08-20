#!/bin/bash

## Create FileSystem Folder Structur

cd /home
mkdir ids
cd /home/ids

## Software dependencies

sudo apt install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-compose \
    maven \
    openjdk-11-jdk \
    ruby 

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo \
  "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo gem install jwt

## Create Certificate Folder Structur

sudo mkdir -p /etc/idscert/localhost

## DataspaceConnector

## Download
cd /home/ids
wget https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/DataspaceConnector/DataspaceConnector-main.zip

## Unzip the folder
mkdir DataspaceConnector
cd DataspaceConnector
unzip ../DataspaceConnector-main.zip
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

cd /home/ids
## Download
wget https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/MetadataBroker/metadata-broker-open-core.zip
mkdir MetadataBroker
cd MetadataBroker
unzip ../metadata-broker-open-core.zip

sudo cp -n certs/broker/server.key /etc/idscert/localhost
sudo cp -n certs/broker/server.crt /etc/idscert/localhost

cd /home/ids/MetadataBroker/metadata-broker-open-core-master/docker/composefiles/broker-localhost
docker-compose pull

## Uncomment the following line if issue appears regarding port 80 already in use

sed -i "10s/80:80/81:80/" docker-compose.yml

gnome-terminal -- docker-compose up

## DAPS

cd /home/ids
## Download
wget https://github.com/International-Data-Spaces-Association/IDS-testbed/blob/master/Testbed/OmejdnDAPS/omejdn-daps.zip
mkdir OmejdnDAPS
cd OmejdnDAPS
unzip ../omejdn-daps.zip
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
