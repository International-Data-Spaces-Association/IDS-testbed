#!/bin/bash
GR=`tput setaf 2`
NC=`tput sgr0`

echo "${GR}Cleaning up the environment for testing the DAPS${NC}"

# Restore server's certs from backup and cleanup testing keys directory
cd .. && rm -r keys && mv keys-backup keys && \
echo "${GR}Restored server certs and deleted testing keys directory${NC}"

# Restore existing DAPS configuration
mv config/clients.yml.orig config/clients.yml && \
echo "${GR}Restored testbed configuration configuration${NC}"

# Remove configuration file for testing
rm tests/test_config.txt && \
echo "${GR}Deleted configuration file for testing${NC}"