Feature: rules karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Rules test cases
    Scenario: GET '/api/rules' create a rule and get all rules with the created one

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

        Given path 'api/rules'
        When method get
        Then status 200
        And match response._embedded.rules contains deep rule
    
    Scenario:  GET '/api/rules/{id}' create a rule and get it by id

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

        Given url rule_id 
        When method get
        Then status 200
        And match response contains rule

    Scenario: PUT '/api/rules/{id}' Create a rule and then change it values

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

        * def rule_id = response._links.self.href

        Given url rule_id 
        And request rule2 
        When method put 
        Then status 204

        Given url rule_id 
        When method get
        Then status 200
        And match response contains rule2 
#Rules and contracts linking test cases
    Scenario: POST '/api/rules/{id}/contracts' create a rule and a contract and link them

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
        * set req[0] = contr_id

        Given url rule_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer", "title": "contr_title"}

    Scenario: PUT '/api/rules/{id}/contracts' create two contracts and a rule, link a rule to the contract and them change the link to the other contract

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
        And request contract1
        When method post
        Then status 201
        And match response contains {"consumer": "contr1_consumer","title": "contr1_title"}

        * def contr1_id = response._links.self.href

        Given path 'api/contracts'
        And request contract2
        When method post
        Then status 201
        And match response contains {"consumer": "contr2_consumer","title": "contr2_title"}

        * def contr2_id = response._links.self.href

        * def req = []
        * set req[0] = contr1_id

        Given url rule_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr1_consumer","title": "contr1_title"}

        * def req = []
        * set req[0] = contr2_id

        Given url rule_id
        And path '/contracts'
        And request req
        When method put
        Then status 204

        Given url rule_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr2_consumer","title": "contr2_title"}      
    Scenario: DELETE '/api/rules/{id}/contracts' create a rule and a contract, link them and them unlink them

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
        * set req[0] = contr_id

        Given url rule_id
        And path '/contracts'
        And request req
        When method post
        Then status 200
        And match response._embedded.contract contains deep {"consumer": "contr_consumer","title": "contr_title"}

        Given url rule_id
        And path '/contracts'
        And request req
        When method delete
        Then status 204

        Given url rule_id
        And path '/contracts'
        When method get
        Then status 200
        And match response._embedded.contract !contains {"consumer": "contr_consumer","title": "contr_title"}

        
