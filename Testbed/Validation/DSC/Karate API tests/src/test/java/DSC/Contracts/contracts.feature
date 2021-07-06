Feature: resource karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Contracts test cases
    Scenario: GET '/api/contracts' create a contract and receive it in the general get function

        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        Given path 'api/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

    Scenario: POST '/api/contracts' create a contract and receive it in the id get function
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        Given url contr_id
        When method get
        Then status 200
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

    Scenario: PUT '/api/contracts' create a contract and change its data 
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

        Given path 'api/contracts'
        And request contract1
        When method post
        Then status 201
        And match response contains {"consumer": "contr1_consumer","title": "contr1_title"}

        * def contr_id = response._links.self.href

        Given url contr_id
        And request contract2
        When method put
        Then status 204

        Given url contr_id
        When method get
        Then status 200
        And match response contains {"consumer": "contr2_consumer","title": "contr2_title"}
#Offer and contract linking test cases
    Scenario: POST '/api/contracts/{id}/offers' create an offer and a contract and link the offer to the contract

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
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = off_id

        Given url contr_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer

        Given url off_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

    Scenario: PUT '/api/contracts/{id}/offers' create two offers and a contract, link the offer to a contract and the change the link to the seccond contract
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
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

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

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = off1_id

        Given url contr_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer1

        Given url off1_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

        * def req = []
        * set req[0] = off2_id

        Given url contr_id
        And path '/offers'
        And request req
        When method put
        Then status 204

        Given url off2_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

    Scenario: DELETE '/api/contracts/{id}/offers' create an offer and a contract, link the offer to the contract and then errase it
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
        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        Given path 'api/offers'
        And request offer
        When method post
        Then status 201
        And match response contains offer

        * def off_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = off_id

        Given url contr_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer

        Given url off_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}


        Given url contr_id
        And path '/offers'
        And request req
        When method delete
        Then status 204

        Given url off_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract !contains deep {"consumer": "contr_consumer","title": "contr_title"}
#Request and contract linking not done due to not post request function

#Rules and contract linking
    Scenario: POST '/api/contracts/{id}/rules' create a rule and a contract and link them

        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        * def rule =
        """
        {
            "title": "rule_title",
            "value": "rule_value"
        }
        """

        Given path 'api/rules'
        And request rule
        When method post
        Then status 201
        And match response contains rule

        * def rule_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = rule_id

        Given url contr_id
        And path '/rules'
        And request req
        When method post
        Then status 200
        And match response._embedded.rules contains deep rule

    Scenario: PUT '/api/contracts/{id}/rules' create two rules and a contract, link a rule to the contract and them change the link to the other rule

        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        * def rule1 =
        """
        {
            "title": "rule1_title",
            "value": "rule1_value"
        }
        """

        * def rule2 =
        """
        {
            "title": "rule2_title",
            "value": "rule2_value"
        }
        """

        Given path 'api/rules'
        And request rule1
        When method post
        Then status 201
        And match response contains rule1

        * def rule1_id = response._links.self.href

        Given path 'api/rules'
        And request rule2
        When method post
        Then status 201
        And match response contains rule2

        * def rule2_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = rule1_id

        Given url contr_id
        And path '/rules'
        And request req
        When method post
        Then status 200
        And match response._embedded.rules contains deep rule1

        * def req = []
        * set req[0] = rule2_id

        Given url contr_id
        And path '/rules'
        And request req
        When method put
        Then status 204

        Given url contr_id
        And path '/rules'
        When method get
        Then status 200
        And match response._embedded.rules contains deep rule2        
    Scenario: DELETE '/api/contracts/{id}/rules' create a rule and a contract, link them and them unlink them

        * def contract =
        """
        {
            "consumer": "contr_consumer",
            "title": "contr_title",
            "start": "2021-06-07T09:14:23.316Z",
            "end": "2021-06-07T09:14:23.316Z"
        }
        """

        * def rule =
        """
        {
            "title": "rule_title",
            "value": "rule_value"
        }
        """

        Given path 'api/rules'
        And request rule
        When method post
        Then status 201
        And match response contains rule

        * def rule_id = response._links.self.href

        Given path 'api/contracts'
        And request contract
        When method post
        Then status 201
        And match response contains {"consumer": "contr_consumer","title": "contr_title"}

        * def contr_id = response._links.self.href

        * def req = []
        * set req[0] = rule_id

        Given url contr_id
        And path '/rules'
        And request req
        When method post
        Then status 200
        And match response._embedded.rules contains deep rule

        Given url contr_id
        And path '/rules'
        And request req
        When method delete
        Then status 204

        Given url contr_id
        And path '/rules'
        When method get
        Then status 200
        And match response._embedded.rules !contains rule
