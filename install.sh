#!/bin/bash
##### Software dependencies
sudo dpkg --configure -a
sudo apt-get install maven
sudo apt-get install docker
sudo apt-get install docker-compose
sudo apt-get install gnome-terminal
sudo apt install openjdk-11-jdk
sudo apt install curl
sudo apt install ruby
sudo gem install jwt
sudo apt install python3-openssl

##### Installation and configuration of components
sudo docker network create broker-localhost_default

#### Omejdn DAPS
cd OmejdnDAPS

### Get started
sudo docker build -t daps .

### Launch the component
if [ ! "$(sudo docker ps -q -f name=omejdn)" ]; then
    if [ "$(sudo docker ps -aq -f status=exited -f name=omejdn)" ]; then
        # cleanup
        sudo docker rm omejdn
    fi
    # run your container
    gnome-terminal --title "DAPS" -e "docker run --name omejdn -p 4567:4567 -v $PWD/config:/opt/config -v $PWD/keys:/opt/keys --network=broker-localhost_default daps"
fi

#### Dataspace Connector A
cd ../DataspaceConnectorA

### Get started
sudo docker build -t dsca .

### Launch the component
if [ ! "$(sudo docker ps -q -f name=connectora)" ]; then
    if [ "$(sudo docker ps -aq -f status=exited -f name=connectora)" ]; then
        # cleanup
        sudo docker rm connectora
    fi
    # run your container
    gnome-terminal --title "CONNECTOR A" -e "docker run --publish 8080:8080 --name connectora --network=broker-localhost_default dsca"
fi

#### Dataspace Connector B
cd ../DataspaceConnectorB

### Get started
sudo docker build -t dscb .

### Launch the component
if [ ! "$(sudo docker ps -q -f name=connectorb)" ]; then
    if [ "$(sudo docker ps -aq -f status=exited -f name=connectorb)" ]; then
        # cleanup
        sudo docker rm connectorb
    fi
    # run your container
    gnome-terminal --title "CONNECTOR B" -e "docker run --publish 8081:8081 --name connectorb --network=broker-localhost_default dscb"
fi

#### Metadata Broker
# Configure TLS certificates
cd ../MetadataBroker
sudo mkdir /etc/idscert/localhost
sudo cp server.crt /etc/idscert/localhost
sudo cp server.key /etc/idscert/localhost
# Check if the required images are available in the local system
if [ "$(sudo docker images -q registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-reverseproxy)" == "" ]; then
# If the images are no available, pull them
    cd docker/composefiles/broker-localhost
    sudo docker-compose pull
# The testbed requires local changes. Remove the pulled "core" image
    sudo docker rmi registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core
# Build a local "core" image with the correct changes
    cd ../../broker-core
    sudo docker build -t registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core .
# Launch the component
    cd ../composefiles/broker-localhost
    gnome-terminal --title "BROKER" -e "docker-compose up"
fi

### Check the components are running
echo "Checking OmejdnDAPS availability..."
sleep 15
echo "Checking Dataspace Connector A availability..."
sleep 15
echo "Checking Dataspace Connector B availability..."
sleep 15
echo "Checking Metadata Broker availability..."
sleep 15

#### Omejdn DAPS
curl -k -s "http://localhost:4567" &>/dev/null && echo $'>\e[1;32m SUCCESS\e[0m - Omejdn DAPS is available: http://localhost:4567' || echo $'>\e[1;31m ERROR\e[0m - Omejdn DAPS is not working'
#### Dataspace Connector A
curl -k -s "https://localhost:8080" &>/dev/null && echo $'>\e[1;32m SUCCESS\e[0m - Dataspace Connector A is available: https://localhost:8080' || echo $'>\e[1;31m ERROR\e[0m - Dataspace Connector A is not working'
#### Dataspace Connector B
curl -k -s "https://localhost:8081" &>/dev/null && echo $'>\e[1;32m SUCCESS\e[0m - Dataspace Connector B is available: https://localhost:8081' || echo $'>\e[1;31m ERROR\e[0m - Dataspace Connector B is not working'
#### Metadata Broker
curl -k -s "https://localhost" &>/dev/null && echo $'>\e[1;32m SUCCESS\e[0m - Metadata Broker is available: https://localhost' || echo $'>\e[1;31m ERROR\e[0m - Metadata Broker is not working'


