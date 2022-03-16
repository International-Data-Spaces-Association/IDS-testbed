FROM postman/newman:latest

WORKDIR /newman

COPY . .

#RUN git clone https://github.com/postmanlabs/newman.git
RUN newman run "TestbedPreconfiguration.postman_collection.json" --folder "Connector" -e "Applicant IDS Connector Test Configuration.postman_environment.json" --insecure
RUN newman run "TestbedPreconfiguration.postman_collection.json" --folder "Broker" -e "Applicant IDS Broker Test Configuration.postman_environment.json" --insecure

EXPOSE 8087
