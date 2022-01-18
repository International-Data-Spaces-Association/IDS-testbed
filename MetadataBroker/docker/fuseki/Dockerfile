FROM stain/jena-fuseki

#Need to install procps to allow the container to run "ps". Otherwise, restarting Fuseki will fail
RUN apt update && apt install -y --no-install-recommends procps

# set password for user "admin"
ENV ADMIN_PASSWORD password

# create the connector self description database
ADD connectorData.ttl /fuseki/configuration/connectorData.ttl