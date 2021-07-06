Feature: resource karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Catalogs test cases
    Scenario: GET '/api/catalogs/' create a catalog and get all catalogs looking for the one created      
        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
        
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog
        
        Given path 'api/catalogs'
        When method get
        Then status 200
        And match response._embedded.catalogs contains deep catalog

    Scenario: GET '/api/catalogs/{id}' create a catalog and then get it by id
        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def id_url = response._links.self.href

        Given url id_url
        When method get 
        Then status 200
        And match response contains catalog
    
    Scenario: PUT '/api/catalogs/{id}' create a catalog and modoify it by its id
        * def catalog1 =
        """
        {
            "title": "catalog1_title",
            "description": "catalog1_description"
        }
        """
        * def catalog2 =
        """
        {
            "title": "catalog2_title",
            "description": "catalog2_description"
        }
        """
        
        Given path 'api/catalogs'
        And request catalog1
        When method post
        Then status 201
        And match response contains catalog1


        * def id_url = response._links.self.href
        
                

        Given url id_url
        And request catalog2
        When method put
        Then status 204
        #And match response contains catalog1

        Given url id_url
        When method get 
        Then status 200
        And match response contains catalog2
        And match response !contains catalog1

    Scenario: DELETE '/api/catalogs/{id}' Create a catalog and then errase it
        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def id_url = response._links.self.href

        Given url id_url
        When method delete 
        Then status 204

        Given url id_url
        When method get
        Then status 404
    
    
    


#Link offers with catalogs
    Scenario: POST '/api/catalogs/{id}/offers' create a catalog, create an offer and link the offer to a catalog

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
        * set req[0] = off_id




        Given url cat_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer

    Scenario: PUT '/api/catalogs/{id}/offers' create two offers, create a  catalog and link the offer to a catalog, then change the link to the second offer

        * def catalog =
        """
        {
            "title": "catalog_title",
            "description": "catalog_description"
        }
        """
        * def offer1 =
        """
        {
            "title": "offer1_title",
            "description": "offer1_description",
            "keywords": [
                "keyword1",
                "keyword2"
            ],
            "publisher": "offer1_publisher",
            "language": "offer1_language",
            "licence": "offer1_licence",
            "sovereign": "offer1_sovereign",
            "endpointDocumentation": "offer1_endpoint"
        }
        """
        * def offer2 =
        """
        {
            "title": "offer2_title",
            "description": "offer2_description",
            "keywords": [
                "keyword1",
                "keyword2"
            ],
            "publisher": "offer2_publisher",
            "language": "offer2_language",
            "licence": "offer2_licence",
            "sovereign": "offer2_sovereign",
            "endpointDocumentation": "offer2_endpoint"
        }
        """
        
        
        Given path 'api/catalogs'
        And request catalog
        When method post
        Then status 201
        And match response contains catalog

        * def cat_id = response._links.self.href

        Given path 'api/offers'
        And request offer1
        When method post
        Then status 201
        And match response contains offer1

        * def off1_id = response._links.self.href

        Given path 'api/offers'
        And request offer2
        When method post
        Then status 201
        And match response contains offer2

        * def off2_id = response._links.self.href
        * def req = []
        * set req[0] = off1_id

        Given url cat_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer1

        * def req = []
        * set req[0] = off2_id

        Given url cat_id
        And path '/offers'
        And request req
        When method put
        Then status 204
        #And match response._embedded.catalogs contains deep catalog2

        Given url cat_id
        And path '/offers'
        When method get
        Then status 200
        And match response._embedded.resources contains deep offer2
    Scenario: DELETE '/api/offers/{id}/catalogs' create a catalog, create an offer and link the offer to a catalog, then errase the offer from the catalog
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
        * set req[0] = off_id




        Given url cat_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer 

        Given url cat_id
        And path '/offers' 
        And request req
        When method delete
        Then status 204

        Given url cat_id
        And path '/offers' 
        When method get
        Then status 200
        And match response._embedded.resources !contains offer 
    
