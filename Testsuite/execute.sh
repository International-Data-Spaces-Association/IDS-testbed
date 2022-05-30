#!/bin/bash
#
# Set up and execute IDS Testsuite

if [ -z "$1" ]; then
    echo "No arguments supplied. Please execute the command again with one valid argument {Connector, Broker}."
fi

currentDate=$(date +'%Y-%m-%d_%T')

newman run "Testsuite.postman_collection.json" --folder $1 -e "env/Applicant_IDS_"$1"_Test_Configuration.postman_environment.json" --insecure -r htmlextra --reporter-htmlextra-title "IDS Testsuite Report" --reporter-htmlextra-export "results/""$currentDate"".html" || exit

cd results
currentInputFile=$(ls -Art | tail -n 1)

outputFile="$1"_"$currentDate"".pdf"
htmldoc --webpage -f $outputFile $currentInputFile

if [ -s "$outputFile" ]
then
   echo " Report generation successful. "
else
   echo " Error! Report generation not successful. File does not exist, or is empty. "
fi
