Feature: resource karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Representations test cases
    Scenario: GET '/api/representations' create a representation and receive it in the general get function

        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """

        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

        Given path 'api/representations'
        When method get
        Then status 200
        And match response._embedded.representations contains deep representation

    Scenario: GET '/api/representations/{id}' create a representation and receive it by id

        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """

        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

        Given url repr_id
        When method get
        Then status 200
        And match response contains representation
    
    Scenario: PUT '/api/representations/{id}' create a representation and change its parameters

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

        Given path 'api/representations'
        And request representation1
        When method post
        Then status 201
        And match response contains representation1

        * def repr_id = response._links.self.href

        Given url repr_id 
        And request representation2
        When method put
        Then status 204

        Given url repr_id 
        When method get
        Then status 200
        And match response contains representation2
        And match response !contains representation1

    Scenario: DELETE '/api/representations/{id}' create a representation and erase it

        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
        }
        """

        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

        Given url repr_id 
        When method delete
        Then status 204

        Given url repr_id 
        When method get
        Then status 404
#Artifacts links to representations
    Scenario: POST '/api/representations/{id}/artifacts' Create an artifact and a representation and link them toghether

        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
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
        * set req[0] = art_id

        Given url repr_id
        Given path '/artifacts'
        And request req
        When method post
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact_title"}

    Scenario: PUT '/api/representations/{id}/artifacts' Create two artifacts and a representation, link the representation to an artifact and then change the link to other artifact 

        * def artifact1 =
        """
        {
            "title": "artifact1_title",
            "value": "artifact1_value"
        }
        """
        * def artifact2 =
        """
        {
            "title": "artifact2_title",
            "value": "artifact2_value"
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

        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

        Given path 'api/artifacts'
        And request artifact1
        When method post
        Then status 201
        And match response contains {"title": "artifact1_title"}

        * def art1_id = response._links.self.href

        Given path 'api/artifacts'
        And request artifact2
        When method post
        Then status 201
        And match response contains {"title": "artifact2_title"}

        * def art2_id = response._links.self.href

        * def req = []
        * set req[0] = art1_id

        Given url repr_id
        Given path '/artifacts'
        And request req
        When method post
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact1_title"}

        * set req[0] = art2_id

        Given url repr_id
        Given path '/artifacts'
        And request req
        When method put
        Then status 204

        Given url repr_id
        Given path '/artifacts'
        When method get
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact2_title"}

    Scenario: DELETE '/api/representations/{id}/artifacts' Create an artifact and a representation, link them toghether and then unlink them

        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
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
        * set req[0] = art_id

        Given url repr_id
        Given path '/artifacts'
        And request req
        When method post
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact_title"}

        Given url repr_id
        Given path '/artifacts'
        And request req
        When method delete
        Then status 204

        Given url repr_id
        Given path '/artifacts'
        When method get
        Then status 200
        And match response._embedded.artifacts !contains {"title": "artifact_title"}


# Offers links to representations 
    Scenario: POST '/api/representations/{id}/offers' Create a representation, create an offer and link the offer to the representation
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
        * set req[0] = off_id

        Given url repr_id
        And path '/offers'
        And request req
        When method post
        Then status 200
        And match response._embedded.resources contains deep offer

    Scenario: PUT '/api/representations/{id}/offers' Create two offers, create a representation and link the offer to a representation, then change it to the other one
        * def representation =
        """
        {
            "title": "repr_title",
            "mediaType": "repr_mediaType",
            "language": "repr_language"
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
        Given path 'api/representations'
        And request representation
        When method post
        Then status 201
        And match response contains representation

        * def repr_id = response._links.self.href

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

        Given url repr_id
        And path '/offers'
        And request req
        When method post
        Then status 200

        Given url repr_id
        And path '/offers'
        When method get
        Then status 200
        And match response._embedded.resources contains deep offer1 

        * def req = []
        * set req[0] = off2_id

        Given url repr_id
        And path '/offers'
        And request req
        When method put
        Then status 204

        Given url repr_id
        And path '/offers'
        When method get
        Then status 200
        And match response._embedded.resources contains deep offer2     

    Scenario: DELETE '/api/representations/{id}/offers'  Create a representation, create an offer, link the offer to the representation and then unlink it
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
        * set req[0] = off_id

        Given url repr_id
        And path '/offers'
        And request req
        When method post
        Then status 200

        Given url repr_id
        And path '/offers'
        When method get
        Then status 200
        And match response._embedded.resources contains deep offer

        Given url repr_id
        And path '/offers'
        And request req
        When method delete
        Then status 204

        Given url repr_id
        And path '/offers'
        When method get
        Then status 200
        And match response._embedded.resources !contains offer

#TODO linking to requests when the post function is implemented


