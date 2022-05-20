FROM postman/newman:latest

WORKDIR /newman

COPY . .

ARG component

RUN if [[ -z "$component" ]] ; then echo "Error: IDS component to test not provided!" ; fi

RUN newman run "Testsuite.postman_collection.json" --folder "$component" -e "env/Applicant_IDS_"$component"_Test_Configuration.postman_environment.json" --insecure

EXPOSE 8087
