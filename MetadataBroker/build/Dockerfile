# Adds the testbed DAPS keystore to the Broker

FROM registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker-open/core:4.2.8
COPY isstbroker-keystore.jks /BOOT-INF/classes/isstbroker-keystore.jks
RUN jar uf *.jar BOOT-INF/classes/isstbroker-keystore.jks
