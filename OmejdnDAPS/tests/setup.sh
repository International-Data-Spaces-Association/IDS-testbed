#!/bin/bash
GR=`tput setaf 2`
NC=`tput sgr0`

echo "${GR}Setting up the environment for testing the DAPS${NC}"

# Backup existing clients and keys
cd .. && mv keys keys-backup && mkdir keys && \
cp config/clients.yml config/clients.yml.orig && \
echo "${GR}Backuped client and keys successfully${NC}"

# Register required connectros for testing
echo "---" > config/clients.yml && \
sh scripts/register_connector.sh test1 >> config/clients.yml && \
sh scripts/register_connector.sh test2 >> config/clients.yml && \
sh scripts/register_ec_connector.sh ec256 256 >> config/clients.yml && \
sh scripts/register_ec_connector.sh ec521 521 >> config/clients.yml && \
echo "${GR}Connectors added successfully and key material${NC}"

# Create config file for testing
ISS="iss=$(awk 'NR==2{ print; exit }' config/clients.yml | cut -c 14-)"
AUD="aud=$(awk 'NR==8{ print; exit }' config/omejdn.yml | cut -c 13-)"
ISS_DAPS="iss_daps=$(awk 'NR==9{ print; exit }' config/omejdn.yml | cut -c 11-)"
SEC="securityProfile=$(awk 'NR==9{ print; exit }' config/clients.yml | cut -c 12-)"
CONN="referringConnector=$(awk 'NR==11{ print; exit }' config/clients.yml | cut -c 12-)"
TYPE="@type=$(awk 'NR==13{ print; exit }' config/clients.yml | cut -c 12-)"
CONT="@context=$(awk 'NR==15{ print; exit }' config/clients.yml | cut -c 12-)"
SCOPE="scope=$(awk 'NR==6{ print; exit }' config/clients.yml | cut -c 5-)"
TRANS="transportCertsSha256=$(awk 'NR==17{ print; exit }' config/clients.yml | cut -c 12-)"
KEY1="keyPath=../keys/test1.key"
KEY2="keyPath2=../keys/test2.key"
ISS2="iss2=$(awk 'NR==19{ print; exit }' config/clients.yml | cut -c 14-)"
URL="url=http://localhost:4567/"
ISS_256="iss_256=$(awk 'NR==36{ print; exit }' config/clients.yml | cut -c 14-)"
SEC_256="securityProfile_256=$(awk 'NR==43{ print; exit }' config/clients.yml | cut -c 12-)"
CONN_256="referringConnector_256=$(awk 'NR==45{ print; exit }' config/clients.yml | cut -c 12-)"
SCOPE_256="scope_256=$(awk 'NR==40{ print; exit }' config/clients.yml | cut -c 5-)"
TRANS_256="transportCertsSha256_256=$(awk 'NR==51{ print; exit }' config/clients.yml | cut -c 12-)"
KEY3="keyPath3=../keys/ec256.key"
ISS_512="iss_512=$(awk 'NR==53{ print; exit }' config/clients.yml | cut -c 14-)"
SEC_512="securityProfile_512=$(awk 'NR==60{ print; exit }' config/clients.yml | cut -c 12-)"
CONN_512="referringConnector_512=$(awk 'NR==62{ print; exit }' config/clients.yml | cut -c 12-)"
SCOPE_512="scope_512=$(awk 'NR==57{ print; exit }' config/clients.yml | cut -c 5-)"
TRANS_512="transportCertsSha256_512=$(awk 'NR==68{ print; exit }' config/clients.yml | cut -c 12-)"
KEY4="keyPath4=../keys/ec521.key"
EC256="${ISS_256}\n${SEC_256}\n${CONN_256}\n${SCOPE_256}\n${TRANS_256}\n${KEY3}"
EC512="${ISS_512}\n${SEC_512}\n${CONN_512}\n${SCOPE_512}\n${TRANS_512}\n${KEY4}"
echo "${ISS}\n${AUD}\n${ISS_DAPS}\n${SEC}\n${CONN}\n${TYPE}\n${CONT}\n${SCOPE}\n${TRANS}\n${KEY1}\n${KEY2}\n${ISS2}\n${URL}\n${EC256}\n${EC512}" > tests/test_config.txt && \
echo "${GR}Configuration file for testing contains:${NC}" && \
cat tests/test_config.txt