FROM postman/newman:latest

WORKDIR /newman

COPY . .

#RUN git clone https://github.com/postmanlabs/newman.git
RUN newman run "TestbedPreconfiguration.postman_collection.json" --folder "Automated Testsuite" -e "Applicant IDS Connector Test Configuration.postman_environment.json"

EXPOSE 8087
