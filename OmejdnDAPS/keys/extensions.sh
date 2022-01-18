#!/bin/bash

# Define the name of your certificate
echo "> Input your certificate filename:"
read a

# Extract the aki/ski extensions automatically
b=$(openssl x509 -in $a -noout -text | grep -A1 "Subject Key Identifier" | tail -1 | tr --delete " ")
c=$(openssl x509 -in $a -noout -text | grep -A3 "Subject Key Identifier" | tail -1 | tr --delete " ")

# Provide the aki/ski extensions to the user
cat $a &>/dev/null && echo "> The aki/ski extension for $a is:
$b:$c"
