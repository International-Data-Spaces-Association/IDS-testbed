Feature: resource karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Offer test cases
    Scenario: GET '/api/offers/' get all offers       
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

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        
        Given path 'api/offers'
        When method get
        Then status 200
        And match response._embedded.resources contains deep offer

    Scenario: POST '/api/offers/' create an offer and then get it by id
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
        Given path 'api/offers'
        And request offer
        When method post
        Then status 201

        * def id_url = response._links.self.href

        Given url id_url
        When method get 
        Then status 200
        And match response contains offer
    
    Scenario: PUT '/api/offers/{id}' create an offer and modoify it by its id

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
        Given path 'api/offers'
        And request offer
        When method post
        Then status 201

        * def id_url = response._links.self.href
        * print 'Created offer in url: ', id_url

        * def offer2 =
        """
        {
            "title": "offer_title2",
            "description": "offer_description2",
            "keywords": [
                "keyword12",
                "keyword22"
            ],
            "publisher": "offer_publisher2",
            "language": "offer_language2",
            "licence": "offer_licence2",
            "sovereign": "offer_sovereign2",
            "endpointDocumentation": "offer_endpoint2"
        }
        """
                

        Given url id_url
        And request offer2
        When method put 
        Then status 204

        Given url id_url
        When method get 
        Then status 200
        And match response contains offer2
    
    
    
#Link offers with catalogs
    Scenario: POST '/api/offers/{id}/catalogs' create a catalog, create an offer and link the offer to a catalog

        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
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
        
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def cat_id = response._links.self.href

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = cat_id

        Given url off_id
        And path '/catalogs'
        And request req
        When method post
        Then status 200
        And match response._embedded.catalogs contains deep catalog

    Scenario: PUT '/api/offers/{id}/catalogs' create two catalogs, create an  offer and link the offer to a catalog, then change the link to the second catalog

        * def catalog1 =
        """
        {
            "title": "catalog_title1",
            "description": "catalog_description1"
        }
        """
        * def catalog2 =
        """
        {
            "title": "catalog_title2",
            "description": "catalog_description2"
        }
        """
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
        
        Given path 'api/catalogs'
        And request catalog1
        When method post
        Then status 201
        And match response contains catalog1

        * def cat_id1 = response._links.self.href

        Given path 'api/catalogs'
        And request catalog2
        When method post
        Then status 201
        And match response contains catalog2

        * def cat_id2 = response._links.self.href

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = cat_id1

        Given url off_id
        And path '/catalogs'
        And request req
        When method post
        Then status 200
        And match response._embedded.catalogs contains deep catalog1

        * def req = []
        * set req[0] = cat_id2

        Given url off_id
        And path '/catalogs'
        And request req
        When method put
        Then status 204
        #And match response._embedded.catalogs contains deep catalog2

        Given url off_id 
        And path '/catalogs'
        When method get
        Then status 200
        And match response._embedded.catalogs contains deep catalog2
    Scenario: DELETE '/api/offers/{id}/catalogs' create a catalog, create an offer and link the offer to a catalog, then errase the catalog from the offer
        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
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
        
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def cat_id = response._links.self.href

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = cat_id




        Given url off_id
        And path '/catalogs'
        And request req
        When method post
        Then status 200
        And match response._embedded.catalogs contains deep catalog 

        Given url off_id
        And path '/catalogs' 
        And request req
        When method delete
        Then status 204

        Given url off_id
        And path '/catalogs' 
        When method get
        Then status 200
        And match response._embedded.catalogs !contains deep catalog 
    
#Link offer with contracts
    Scenario: POST '/api/offers/{id}/contracts' Create a contract, create an offer and link the offer to the contract
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """
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
        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains ({consumer: ''+contract.consumer , title: ''+contract.title})

        * def contr_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = contr_id

        Given url off_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}
    
    Scenario: PUT '/api/offers/{id}/contracts' Create two contracts, create an offer and link the offer to a contract, then change it to another contract
        * def contract1 =
        """
        {
            "consumer": "contr1_consumer",
            "title": "contr1_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """
         * def contract2 =
        """
        {
            "consumer": "contr2_consumer",
            "title": "contr2_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """
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
        Given path 'api/contracts'
        And request contract1
        When method post
        Then status 201
        #And match response contains deep contract

        * def contr1_id = response._links.self.href

        Given path 'api/contracts'
        And request contract2
        When method post
        Then status 201
        #And match response contains deep contract

        * def contr2_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = contr1_id

        Given url off_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr1_consumer","title": "contr1_title"}
    

        * def req = []
        * set req[0] = contr2_id

        Given url off_id
        And path '/contracts'
        And request req
        When method put
        Then status 204


        Given url off_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr2_consumer","title": "contr2_title"}
    
    Scenario: DELETE '/api/offers/{id}/contracts' Create a contract, create an offer, link the offer to the contract and then errase the contract
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """
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
        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        #And match response contains deep contract

        * def contr_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = contr_id

        Given url off_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

        Given url off_id
        And path '/contracts'
        And request req
        When method delete
        Then status 204

        Given url off_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract !contains {"consumer": "contr2_consumer","title": "contr2_title"}



#Link offer with representations
    Scenario: POST '/api/offers/{id}/representations' Create a representation, create an offer and link the offer to the representation
        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """
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
        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = repr_id

        Given url off_id
        And path '/representations'
        And request req
        When method post
        Then status 200
        And match response._embedded.representations contains deep representation

    Scenario: PUT '/api/offers/{id}/representations' Create two representations, create an offer and link the offer to a representation, then change it to the other one
        * def representation1 =
        """
        {
            "title": "repr1_title",
            "mediaType": "repr1_mediaType",
            "language": "repr1_language"
        }
        """
         * def representation2 =
        """
        {
            "title": "repr2_title",
            "mediaType": "repr2_mediaType",
            "language": "repr2_language"
        }
        """
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
        Given path 'api/representations'
        And request representation1
        When method post
        Then status 201
        #And match response contains representation

        * def repr1_id = response._links.self.href

        Given path 'api/representations'
        And request representation2
        When method post
        Then status 201
        #And match response contains representation

        * def repr2_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = repr1_id

        Given url off_id
        And path '/representations'
        And request req
        When method post
        Then status 200

        Given url off_id
        And path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations contains deep representation1 

        * def req = []
        * set req[0] = repr2_id

        Given url off_id
        And path '/representations'
        And request req
        When method put
        Then status 204

        Given url off_id
        And path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations contains deep representation2     

    Scenario: DELETE '/api/offers/{id}/representations'  Create a representation, create an offer, link the offer to the representation and then unlink it
       * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """
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
        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        #And match response contains representation

        * def repr_id = response._links.self.href


        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href
        * def req = []
        * set req[0] = repr_id

        Given url off_id
        And path '/representations'
        And request req
        When method post
        Then status 200

        Given url off_id
        And path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations contains deep representation

        Given url off_id
        And path '/representations'
        And request req
        When method delete
        Then status 204

        Given url off_id
        And path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations !contains representation

#Complete an offer links with  catalog and representation (with an artifact)
    # Scenario: Create an offer, a catalog, a contract and a representation and link all of them toghether. Then the offer should be completed
    #     * def catalog =
    #     """
    #     {
    #         "title": "catalog_title",
    #         "description": "catalog_description"
    #     }
    #     """
    #     * def representation =
    #     """
    #     {
    #         "title": "repr_title",
    #         "mediaType": "repr_mediaType",
    #         "language": "repr_language"
    #     }
    #     """
    #     * def artifact =
    #     """
    #     {
    #         "title": "art_title",
    #         "accessUrl": "art_accessUrl",
    #         "username": "art_username",
    #         "password": "art_password",
    #         "value": "art_value",
    #         "automatedDownload": true
    #     }
    #     """
    #     * def offer =
    #     """
    #     {
    #         "title": "offer_title",
    #         "description": "offer_description",
    #         "keywords": [
    #             "keyword1",
    #             "keyword2"
    #         ],
    #         "publisher": "offer_publisher",
    #         "language": "offer_language",
    #         "licence": "offer_licence",
    #         "sovereign": "offer_sovereign",
    #         "endpointDocumentation": "offer_endpoint"
    #     }
    #     """

    #     Given path 'api/catalogs'
    #     And request catalog
    #     When method post
    #     Then status 201
    #     And match response contains catalog

    #     * def cat_id = response._links.self.href

    #     Given path 'api/contracts'
    #     And request contract
    #     When method post
    #     Then status 201
    #     #And match response contains deep contract

    #     * def contr_id = response._links.self.href

    #     Given path 'api/representations'
    #     And request representation
    #     When method post
    #     Then status 201
    #     #And match response contains representation

    #     * def repr_id = response._links.self.href

    #     Given path 'api/artifacts'
    #     And request artifact
    #     When method post
    #     Then status 201
    #     And match response contains artifact

    #     * def art_id = 

    #     Given path 'api/offers'
    #     And request offer
    #     When method post
    #     Then status 201
    #     And match response contains offer

    #     * def off_id = response._links.self.href

    #     #Link the catalog to the offer
    #     * def req = []
    #     * set req[0] = cat_id

    #     Given url off_id
    #     And path '/catalogs'
    #     And request req
    #     When method post
    #     Then status 200
    #     And match response._embedded.catalogs contains deep catalog

    #     #Link the representation to the offer
    #     * def req = []
    #     * set req[0] = repr_id

    #     Given url off_id
    #     And path '/representations'
    #     And request req
    #     When method post
    #     Then status 200
    #     And match response._embedded.representations contains deep representation









    
        