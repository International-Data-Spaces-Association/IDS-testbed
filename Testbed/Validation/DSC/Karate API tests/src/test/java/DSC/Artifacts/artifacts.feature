Feature: resource karate test script
    for help, see: https://github.com/intuit/karate/wiki/IDE-Support

    Background:
    * configure ssl = true
    * def Auth = call read('../../basic-auth.js') { username: 'admin', password: 'password' }
    * configure headers = ({Authorization: ''+Auth})
    Given url envurl 

    #Artifact test cases
    Scenario: GET '/api/artifacts' create an artifact and get all artifacts finding for it       
        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        Given path 'api/artifacts'
        When method get
        Then status 200
        And match response._embedded.artifacts contains deep {"title": "artifact_title"}

    Scenario: GET '​/api​/artifacts​/{id}' create an artifact and get it by id
        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def id_url = response._links.self.href

        Given url id_url
        When method get
        Then status 200 
        And match response contains {"title": "artifact_title"}
    
    Scenario: PUT '​/api​/artifacts​/{id}' Create an artifact, then change its parameters

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

        Given path 'api/artifacts'
        And request artifact1
        When method post
        Then status 201
        And match response contains {"title": "artifact1_title"}

        * def id_url = response._links.self.href

        Given url id_url
        And request artifact2
        When method put 
        Then status 204

        Given url id_url
        When method get 
        Then status 200
        And match response contains {"title": "artifact2_title"}

    Scenario: DELETE '​/api​/artifacts​/{id}'  Create an artifact and then delete it
        
        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def id_url = response._links.self.href

        Given url id_url
        When method delete
        Then status 204

        Given url id_url
        When method get
        Then status 404

#TODO validar las funciones de agreements con los artifacts cuando las funciones de creación esten implementadas
#TODO archivo de features de agreeements
# Data test cases
    Scenario: GET '​/api​/artifacts​/{id}​/data​/**' Create an artifact, put some data in it and read the data with the get function

        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """
        * def data =
        """
        {
            "headers": {
                "header_key": "data_header"
            },
            "params": {
                "param_key": "data_params"
            },
            "pathVariables": {
                "pathVar_key": "data_pathVariables"
            }
        }
        """

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def id_url = response._links.self.href

        Given url id_url
        And path '/data'
        And request data
        When method post
        Then status 200

        Given url id_url
        And path '/data/**'
        When method get
        Then status 200

    Scenario: PUT '​/api​/artifacts​/{id}​/data​/' Create an anrtifact data and modify it

        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """
        * def data1 =
        """
        {
            "headers": {
                "header_key": "data1_header"
            },
            "params": {
                "param_key": "data1_params"
            },
            "pathVariables": {
                "pathVar_key": "data1_pathVariables"
            }
        }
        """
        * def data2 =
        """
        {
            "headers": {
                "header_key": "data2_header"
            },
            "params": {
                "param_key": "data2_params"
            },
            "pathVariables": {
                "pathVar_key": "data2_pathVariables"
            }
        }
        """
        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def id_url = response._links.self.href

        Given url id_url
        And path '/data'
        And request data1
        When method post
        Then status 200

        Given url id_url
        And path '/data/**'
        When method get
        Then status 200

        Given url id_url
        And path '/data'
        And request data1
        When method put
        Then status 200

        Given url id_url
        And path '/data/**'
        When method get
        Then status 200



#Representation link with artifacts
    Scenario: POST '/api/artifacts/{id}/representations' Create an artifact and a representation and link them toghether

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
        * set req[0] = repr_id

        Given url art_id
        Given path '/representations'
        And request req
        When method post
        Then status 200
        And match response._embedded.representations contains deep representation

    Scenario: PUT '/api/artifacts/{id}/representations' Create two representations and an artifact, link a representation to the artifact and then change the link to other representation 

        * def artifact =
        """
        {
            "title": "artifact_title",
            "value": "artifact_value"
        }
        """
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

        * def repr1_id = response._links.self.href

        Given path 'api/representations'
        And request representation2
        When method post
        Then status 201
        And match response contains representation2

        * def repr2_id = response._links.self.href

        Given path 'api/artifacts'
        And request artifact
        When method post
        Then status 201
        And match response contains {"title": "artifact_title"}

        * def art_id = response._links.self.href

        * def req = []
        * set req[0] = repr1_id

        Given url art_id
        Given path '/representations'
        And request req
        When method post
        Then status 200
        And match response._embedded.representations contains deep representation1

        * set req[0] = repr2_id

        Given url art_id
        Given path '/representations'
        And request req
        When method put
        Then status 204

        Given url art_id
        Given path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations contains deep representation2

    Scenario: DELETE '/api/artifacts/{id}/representations' Create an artifact and a representation, link them toghether and then unlink them

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
        * set req[0] = repr_id

        Given url art_id
        Given path '/representations'
        And request req
        When method post
        Then status 200
        And match response._embedded.representations contains deep representation

        Given url art_id
        Given path '/representations'
        And request req
        When method delete
        Then status 204

        Given url art_id
        Given path '/representations'
        When method get
        Then status 200
        And match response._embedded.representations !contains representation
