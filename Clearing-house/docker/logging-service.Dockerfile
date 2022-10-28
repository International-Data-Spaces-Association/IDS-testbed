#########################################################################################
#
# Builds minimal runtime environment for the Trackchain API
# Copyright 2019 Fraunhofer AISEC
#
#########################################################################################
FROM debian:bullseye-slim

RUN apt-get update \
&& echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections \
&& apt-get --no-install-recommends install -y -q ca-certificates gnupg2 libssl1.1 libc6

# trust the DAPS certificate
COPY docker/daps_cachain.crt /usr/local/share/ca-certificates/daps_cachain.crt
RUN update-ca-certificates

RUN mkdir /server
WORKDIR /server

COPY clearing-house-app/target/release/logging-service .
COPY docker/entrypoint.sh .

ENTRYPOINT ["/server/entrypoint.sh"]
CMD ["/server/logging-service"]
