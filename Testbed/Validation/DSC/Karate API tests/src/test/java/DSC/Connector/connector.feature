Feature: rules karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Usage Control test cases
    Scenario:  PUT '/api/configuration' get the configuration, change it with put and confirm the change

        * def config1 =
        """
        {
            "@context": {
                "ids": "https://w3id.org/idsa/core/",
                "idsc": "https://w3id.org/idsa/code/"
            },
            "@type": "ids:ConfigurationModel",
            "ids:keyStore": {
                "@id": "file:///conf/keystore-localhost.p12"
            },
            "ids:trustStore": {
                "@id": "file:///conf/truststore.p12"
            },
            "ids:connectorStatus": {
                "@id": "idsc:CONNECTOR_OFFLINE"
            },
            "ids:configurationModelLogLevel": {
                "@id": "idsc:MINIMAL_LOGGING"
            },
            "ids:connectorDeployMode": {
                "@id": "idsc:TEST_DEPLOYMENT"
            },
        }
        """

        * def config2 =
        """
        {
            "@context": {
                "ids": "https://w3id.org/idsa/core/",
                "idsc": "https://w3id.org/idsa/code/"
            },
            "@type": "ids:ConfigurationModel",
            "ids:keyStore": {
                "@id": "file:///conf/keystore-localhost.p12"
            },
            "ids:trustStore": {
                "@id": "file:///conf/truststore.p12"
            },
            "ids:connectorStatus": {
                "@id": "idsc:CONNECTOR_OFFLINE"
            },
            "ids:configurationModelLogLevel": {
                "@id": "idsc:MINIMAL_LOGGING"
            },
            "ids:connectorDeployMode": {
                "@id": "idsc:PRODUCTIVE_DEPLOYMENT"
            },
        }
        """

        Given path 'api/configuration'
        When method get
        Then status 200 

        * def config = response
        

        Given path 'api/configuration'
        And request config1
        When method put
        Then status 200

        Given path 'api/configuration'
        When method get
        Then status 200 
        And match response contains config1

        Given path 'api/configuration'
        And request config2
        When method put
        Then status 200

        Given path 'api/configuration'
        When method get
        Then status 200 
        And match response contains config2

        Given path 'api/configuration'
        And request config
        When method put
        Then status 200
        
    Scenario: confirm connector info displays full especification and complete resources

        * def offer =
       """
        {
            "title": "offer_title",
            "description": "offer_description",
            "keywords": [
                "keyword1",
                "keyword2"
            ],
            "publisher": "offer_publisher",
            "language": "offer_language",
            "licence": "offer_licence",
            "sovereign": "offer_sovereign",
            "endpointDocumentation": "offer_endpoint"
        }
        """
        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """
        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """
         * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """
        Given path 'api/offers'
        And request offer
        When method post
        Then status 201

        * def off_id = response._links.self.href

        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def cat_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {consumer: '#(contract.consumer)',title: '#(contract.title)',start: '#ignore',end: '#ignore'}

        * def contr_id = response._links.self.href

        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def art_id = response._links.self.href

        * def req = []
        * set req[0] = cat_id

        Given url off_id
        And path '/catalogs'
        And request req
        When method post
        Then status 200
        And match response._embedded.catalogs contains deep catalog

        * set req[0] = contr_id

        Given url off_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}
    
        * set req[0] = repr_id

        Given url off_id
        And path '/representations'
        And request req
        When method post
        Then status 200
        And match response._embedded.representations contains deep representation

        * set req[0] = art_id

        Given url repr_id
        And path '/artifacts'
        And request req
        When method post
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact_title"}

        * def output = ({"@id": ""+cat_id})
        Given url envurl
        And path 'api/connector'
        When method get
        Then status 200
        And match response.ids:resourceCatalog contains deep output

       