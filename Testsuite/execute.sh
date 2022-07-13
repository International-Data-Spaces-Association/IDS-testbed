#!/bin/bash
#
# Set up and execute IDS Testsuite

if [ -z "$1" ]; then
    echo "No arguments supplied. Please execute the command again with one valid argument {Connector, Broker}."
    exit 1
fi

currentDate=$(date +'%Y-%m-%d_%T')
purgedCurrentInputFile=$currentDate"_purgedCurrentInputFile.html"

echo "Start execution of IDS-Testsuite ..."

newman run "Testsuite.postman_collection.json" --folder $1 -e "env/Applicant_IDS_"$1"_Test_Configuration.postman_environment.json" --insecure -r html --reporter-html-title "IDS Testsuite Report" --reporter-html-export "results/""$currentDate"".html" || exit

cd results
currentInputFile=$(ls -Art | tail -n 1)

# remove script tags in html file to avoid error in generating the pdf report
perl -0777 -pe 's/<script.*?script>//gs' $currentInputFile > $purgedCurrentInputFile

echo "Start creation of report ..."

outputFile="$1"_"$currentDate"".pdf"
htmldoc --webpage -f $outputFile $purgedCurrentInputFile

if [ -s "$outputFile" ]
then
   echo " Report generation successful. "
else
   echo " Error! Report generation not successful. File does not exist, or is empty. "
fi

rm $purgedCurrentInputFile
