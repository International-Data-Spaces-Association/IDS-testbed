#!/bin/bash

if [ -n "$1" ]; then
        VERSION=$1
else
        echo -n "Enter version: "
        read VERSION
fi

##Tag the images
# core
docker tag registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core:$VERSION
echo "taged image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core:$VERSION"

# reverseproxy
docker tag registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/fuseki registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/fuseki:$VERSION
echo "taged image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/fuseki:$VERSION"

# reverseproxy
docker tag registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/reverseproxy registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/reverseproxy:$VERSION
echo "taged image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/reverseproxy:$VERSION"

##Push the images
docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core:$VERSION
docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/fuseki:$VERSION
docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/reverseproxy:$VERSION

docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/core:latest
docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/fuseki:latest
docker push registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker-open/reverseproxy:latest
