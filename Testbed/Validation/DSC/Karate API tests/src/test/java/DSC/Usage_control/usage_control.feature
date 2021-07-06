Feature: rules karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    Scenario: Change the policy and then recieve it in the /api/examples/validation

        * def policy = 'PROVIDE_ACCESS'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'PROHIBIT_ACCESS'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'N_TIMES_USAGE'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'DURATION_USAGE'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'USAGE_DURING_INTERVAL'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'USAGE_UNTIL_DELETION'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'USAGE_LOGGING'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'USAGE_NOTIFICATION'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'CONNECTOR_RESTRICTED_USAGE'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

        * def policy = 'PROVIDE_ACCESS'
        Given path 'api/examples/policy'
        And param type = policy
        When method post
        Then status 200


        Given path 'api/examples/validation'
        And request response 
        When method post
        Then status 200
        And match response == '"'+policy+'"'

    Scenario: Confirm change on /api/configuration/pattern

        * def estado = true

        Given path 'api/configuration/pattern'
        And param status = estado
        When method put
        Then status 200
        
        Given path 'api/configuration/pattern'
        When method get
        Then status 200
        And match response.status == estado

        * def estado = false

        Given path 'api/configuration/pattern'
        And param status = estado
        When method put
        Then status 200
        
        Given path 'api/configuration/pattern'
        When method get
        Then status 200
        And match response.status == estado

    Scenario: Confirm change on /api/configuration/negotiation

        * def estado = true

        Given path 'api/configuration/negotiation'
        And param status = estado
        When method put
        Then status 200
        
        Given path 'api/configuration/negotiation'
        When method get
        Then status 200
        And match response.status == estado

        * def estado = false

        Given path 'api/configuration/negotiation'
        And param status = estado
        When method put
        Then status 200
        
        Given path 'api/configuration/negotiation'
        When method get
        Then status 200
        And match response.status == estado