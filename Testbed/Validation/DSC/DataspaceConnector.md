# DataspaceConnector individual validation

## Documentation

The documentation revised on the DSC is focused on three main core functionalities:

* Installation and deployment documentation. Ensuring that the installation procedure of the component is correctly documented and that when following it, the component correctly gets build and launched.
* API documentation. Ensure it works as expected with the Karate framework as explained in [API Validation](#API-Validation)
* Communication documentation - Ensure the communication process is explained and that if you follow the steps you can communicate between two connectors.

## Code validation

For testing the code quality, we have used a tool called Sonarqube. It analyses the whole project looking for possible bugs and security hotspots, as well as vulnerabilities. By using this tool, we can have a good understanding of the code's quality and its flaws.

## API Validation

For the validation of the functionalities of the DSC's API an open-source framework called Karate was used, based on java and javascript. This tool combines lots of testing capabilities in a unified framework, but the used for this validation has mostly been focused on the API test-automation features.

The API has been tested against its documentation. Making sure every call on the API works as expected for both the response expected and the creation, modification or links of the different components being executed as expected based on the documentation.

The reports of the tests are in the folder Karate-API-tests/target/karate-reports.
