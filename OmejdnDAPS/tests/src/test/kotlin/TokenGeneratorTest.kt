import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import java.util.Date;
import java.io.File;
import kotlin.system.exitProcess;
import java.lang.System;

 /*Note:
  - When passing a private key to omejdn, we need to pass either the full path to the key from the omejd.rb file e.g. /keys/daps.key
    in the config file (omejdn.yml) or the private key should be put in the directory where the omejdn.rb file is located
*/

/*TODO:
- Get arguments to run tests automatically (script) 
    - require
      - keys (2 rsa and 2 ec) 
      - iss -> client id
      - aud -> accept audience
      - iss_daps -> host
      - aud_daps -> accept audicence
*/

@TestInstance(Lifecycle.PER_CLASS)
class TokenGeneratorTest {
 
    private lateinit var generator: TokenGenerator
    private val prop : List<String> = File("test_config.txt").readLines()
    private var keyPath : String = "";
    private var keyPath2 : String = "";
    private var aud : String = "";
    private var iss : String = "";
    private var sub : String = "";
    private var now : Long = 0;
    private var iat : Long = 0;
    private var nbf : Long = 0;
    private var exp : Long = 0;
    private var iss2 : String = "";
    private var aud_daps : String = "";
    private var iss_daps : String = "";
    private var securityProfile : String = "";
    private var referringConnector : String = "";
    private var type : String = "";
    private var context : String = "";
    private var scope : String = "";
    private var transCert : String = "";

    @BeforeAll
    fun setup(){
        // Get required properties for tests to work
        iss = prop.get(0).split("=")[1];
        if(iss.isEmpty()){
            println("Issuer argument is empty");
            System.exit(-1);
        }
        sub = iss;

        aud = prop.get(1).split("=")[1];
        if(aud.isEmpty()){
            println("Audience argument is empty");
            System.exit(-1);
        }
        aud_daps = aud;

        iss_daps = prop.get(2).split("=")[1];
        if(iss_daps.isEmpty()){
            println("Audience2 argument is empty");
            System.exit(-1);
        }

        securityProfile = prop.get(3).split("=")[1];
        if(securityProfile.isEmpty()){
            println("securityProfile argument is empty");
            System.exit(-1);
        }

        referringConnector = prop.get(4).split("=")[1];
        if(referringConnector.isEmpty()){
            println("referringConnector argument is empty");
            System.exit(-1);
        }

        context = prop.get(6).split("=")[1];
        if(context.isEmpty()){
            println("Context argument is empty");
            System.exit(-1);
        }

        scope = prop.get(7).split("=")[1];
        if(scope.isEmpty()){
            println("Scope argument is empty");
            System.exit(-1);
        }

        transCert = prop.get(8).split("=")[1];
        if(transCert.isEmpty()){
            println("transportCertsSha256 argument is empty");
            System.exit(-1);
        }

        iss2 = prop.get(11).split("=")[1];
        if(iss2.isEmpty()){
            println("Issuer2 argument is empty");
            System.exit(-1);
        }

        // Get path for keys to use for jwts
        keyPath = prop.get(9).split("=")[1];
        if(keyPath.isEmpty()){
            println("Key argument is empty");
            System.exit(-1);
        }

        keyPath2 = prop.get(10).split("=")[1];
        if(keyPath2.isEmpty()){
            println("Key2 argument is empty");
            System.exit(-1);
        }

    }

    @BeforeEach
    fun configureSystemUnderTest() {

        type = prop.get(5).split("=")[1];
        if(type.isEmpty()){
            println("Type argument is empty");
            System.exit(-1);
        }

        generator = TokenGenerator();
        now = Date().getTime() / 1000; // Divide by 1000 bc it is given in ms
        iat = now;
        nbf = now;
        exp = now + 3600;
    }

    private fun print_test(text : String){
        println("\u001B[44m\u001B[30mTest Case: "+text+"\u001B[0m\n");
    }
    
    @Test
    @DisplayName("Requests and verifies an access_token")
    /* Base case, this is the norm and expected on how the server correctly gives
       back an access_token that the user can verify and utilize */
    fun getAccessTokenAndVerify() {
        print_test("Requests and verifies an access_token");

        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }
    
    @Test
    @DisplayName("Requests and verifies an access_token with the RSA512 algorithm")
    /* Base case, this is the norm and expected on how the server correctly gives
       back an access_token that the user can verify and utilize */
    fun getAccessTokenAndVerifyRSA512() {
        print_test("Requests and verifies an access_token with the RSA512 algorithm");
        
        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS512");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Requests and verifies an access_token with the ES256 algorithm")
    /* Base case, this is the norm and expected on how the server correctly gives
       back an access_token that the user can verify and utilize */
    fun getAccessTokenAndVerifyES256() {
        print_test("Requests and verifies an access_token with the ES256 algorithm");
        //Variables to feed to the token
        val iss : String = prop.get(13).split("=")[1];
        if(iss.isEmpty()){
            println("Key3 argument is empty");
            System.exit(-1);
        }
        val sub : String =  iss;
        val keyPathEC256 : String = prop.get(18).split("=")[1];
        if(keyPathEC256.isEmpty()){
            println("Key3 argument is empty");
            System.exit(-1);
        }

        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPathEC256, "ES256");
        assertTrue("access_token" in response);

        //Variables to verify the expected token
        type = "ids:DatPayload";
        val securityProfile : String = prop.get(14).split("=")[1];
        if(securityProfile.isEmpty()){
            println("securityProfile argument is empty");
            System.exit(-1);
        }
        val referringConnector : String = prop.get(15).split("=")[1];
        if(referringConnector.isEmpty()){
            println("referringConnector argument is empty");
            System.exit(-1);
        }

        val scope : String = prop.get(16).split("=")[1];
        if(scope.isEmpty()){
            println("Scope argument is empty");
            System.exit(-1);
        }

        val transCert : String = prop.get(17).split("=")[1];
        if(transCert.isEmpty()){
            println("transportCertsSha256 argument is empty");
            System.exit(-1);
        }
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Requests and verifies an access_token with the ES512 algorithm")
    /* Base case, this is the norm and expected on how the server correctly gives
       back an access_token that the user can verify and utilize */
    fun getAccessTokenAndVerifyES512() {
        print_test("Requests and verifies an access_token with the ES512 algorithm");
        //Variables to feed to the token
        val iss : String = prop.get(19).split("=")[1];
        if(iss.isEmpty()){
            println("Key3 argument is empty");
            System.exit(-1);
        }
        val sub : String =  iss;
        val keyPathEC512 : String = prop.get(24).split("=")[1];
        if(keyPathEC512.isEmpty()){
            println("Key4 argument is empty");
            System.exit(-1);
        }

        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPathEC512, "ES512");
        assertTrue("access_token" in response);

        //Variables to verify the expected token
        type = "ids:DatPayload";
        val securityProfile : String = prop.get(20).split("=")[1];
        if(securityProfile.isEmpty()){
            println("securityProfile argument is empty");
            System.exit(-1);
        }
        val referringConnector : String = prop.get(21).split("=")[1];
        if(referringConnector.isEmpty()){
            println("referringConnector argument is empty");
            System.exit(-1);
        }

        val scope : String = prop.get(22).split("=")[1];
        if(scope.isEmpty()){
            println("Scope argument is empty");
            System.exit(-1);
        }

        val transCert : String = prop.get(23).split("=")[1];
        if(transCert.isEmpty()){
            println("transportCertsSha256 argument is empty");
            System.exit(-1);
        }
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Requests and verifies an access_token with the PS256 algorithm")
    /* This test fails because in the DAPS PS256 is not a supported algorithm */
    fun getAccessTokenAndVerifyPS256() {
        print_test("Requests and verifies an access_token with the PS256 algorithm");

        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "PS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Requests and verifies an access_token with the PS512 algorithm")
    /* This test fails because in the DAPS PS512 is not a supported algorithm */
    fun getAccessTokenAndVerifyPS512() {
        print_test("Requests and verifies an access_token with the PS512 algorithm");

        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "PS512");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("NBF set to current time, IAT one hour before and EXP one hour in the future")
    /*  This test successfully creates an access_token. In theory this should not affect the security
        of the token because this behaviour can't really be abused. */
    fun futureNBFToken() {
        print_test("NBF set to one hour in the future after IAT but before EXP");

        // Variables to feed to the token
        val now : Long = Date().getTime() / 1000; // Divide by 1000 bc it is given in ms
        val iat : Long = now - 3600;
        val nbf : Long = now;
        val exp : Long = now + 3600;
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("IAT set to current time, NBF set one hour in the past, EXP one hour in the future")
    /* This test successfully creates an access_token. In theory this should not affect the security
       of the token because this behaviour can't really be abused. */
    fun pastNBFToken() {
        print_test("IAT set to current time, NBF set one hour in the past, EXP one hour in the future");

        // Variables to feed to the token
        val now : Long = Date().getTime() / 1000; // Divide by 1000 bc it is given in ms
        val iat : Long = now;
        val nbf : Long = now - 3600;
        val exp : Long = now + 3600;
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Wrong context")
    /* This test should in theory fail because the given @context is wrong and does not match the
       specified context in the documentation, however it successfully creates an access_token. This implies
       that the context from the token requesting authorization is never checked. In theory, this
       should not be dangerous as the permission for access_token does not depend on the context
       property but it is a bug that could lead to confusion (and if it is not being checked and
       taken into account, what is even the purpose of it?) */
    fun wrongContext() {
        print_test("Wrong context");

        // Variables to feed to the token
        var context : String = "invalid_context";
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        context = "https://w3id.org/idsa/contexts/context.jsonld";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Wrong type")
    /* This test should in theory fail because it contains an invalid @type according to the
       specification, however it successfully creates an access_token. In theory, this
       should not be dangerous as the permission for access_token does not depend on the type
       property but it is a bug that could lead to confusion (and if it is not being checked and
       taken into account, what is even the purpose of it?) */
    fun wrongType() {
        print_test("Wrong type");

        // Variables to feed to the token
        type = "invalid_type";
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertTrue("access_token" in response);

        // Variables to verify the expected token
        type = "ids:DatPayload";
        assertTrue(generator.verifyTokenRequest(response, iss_daps, aud_daps, sub, context, type,
                   securityProfile, referringConnector, scope, transCert));
    }

    @Test
    @DisplayName("Wrong subject")
    /* This test fails as expected. Would be a big vulnerability if a valid access_token
       would be granted for this JWT request token*/
    fun wrongSubject() {
        print_test("Wrong subject");

        // Variables to feed to the token
        val sub : String = "invalid_subject";

        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Wrong audience")
    /* This test fails as expected. As seen in the discussion from the chatgroup this value
       is not really correctly implemented. The error given does not relate the client itself
       but the audience, so the error message is not completely correct. */
    fun wrongAudience() {
        print_test("Wrong audience");

        // Variables to feed to the token
        val aud : String = "invalid_audience";

        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Wrong issuer")
    /* This test fails as expected. Would be a big vulnerability if a valid access_token
       would be granted for this JWT request token*/
    fun wrongIssuer() {
        print_test("Wrong issuer");
        // Variables to feed to the token
        val iss : String = "invalid_issuer";

        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("'alg = none' attack")
    /* https://www.chosenplaintext.ca/2015/03/31/jwt-algorithm-confusion.html This test fails as
       expected. The code in Client.extract_jwt_cid is responsible for this as it always checks
       for the algorithm with which the JWT has been signed and only allows the algorithms RS256,
       RS512, ES256a and ES512. This prevents the "alg = none" attack. Changing to an HMAC algorithm
       would also result in the same behaviour as HMAC algorithms are not acccepted by the DAPS. The
       error message in this test is misleading, as it says the client is not known but the problem
       is the signing algoritm is not accepted in the DAPS. */
    fun getAlgNone() {
        print_test("Attempts to get an access_token with no valid signature algorithm");
        
        // Try to get access_token with the given arguments
        val response : String = generator.getTokenAlgNone(iss, aud, sub, context, type, iat, nbf, exp);
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("JSON injection attack - expiration change")
    /* https://www.acunetix.com/blog/web-security-zone/what-are-json-injections/ Input seems to be sanitized.
       The only interesting field to influence is the "sub" field. All the other fields cannot influence the
       access_token that is created from the server because the DAPS server recalculates these fields or fills
       them in with different information. There is a check for the "iss" and "sub" to be equal in the server, thus
       in this case we set both to the same string that tries to perform the injection. %22,%22exp%22:1893456000
       is the appended string to the "sub", this attempts to create a new field with the following appended form: '"exp":1893456000'
       being 1893456000 an epoch time that we want to set for the exp in the access_token that we might receive.
       ERROR: Client cid%22,%22exp%22:1893456000 does not exist
       is the ERROR displayed in the server, indicating that it could not find the client as it is correctly sanitized
       and there are no parsing vulnerabilities*/
    fun getJSONInjectionAttackExpiration() {
        print_test("Attempts to get an access_token with an specific expiration date"); //1893456000 - Tuesday, 1 January 2030 0:00:00

        // Variables to feed to the token
        val subInjExp : String = sub+"%22,%22exp%22:1893456000";
        val issInjExp: String = subInjExp;

        val response : String = generator.getToken(issInjExp, aud, subInjExp, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("JSON injection attack - audience change")
    /*  %22,%22aud%22:%22desiredAudience%22 is the appended string to the "sub", this attempts to create a new field with the following appended form: '"aud":"desiredAudience"'
       being desiredAudience an audience that we might want access to but we might not be able to access it.
       ERROR: Client cid%22,%22aud%22:%22desiredAudience%22 does not exist
       is the ERROR displayed in the server, indicating that it could not find the client as it is correctly sanitized
       and there are no parsing vulnerabilities. */
    fun getJSONInjectionAttackAudience() {
        print_test("Attempts to get an access_token with an specific user-controlled audience");

        // Variables to feed to the token
        val subInjAud : String = sub+"%22,%22aud%22:%22desiredAudience%22";
        val issInjAud : String = subInjAud;

        val response : String = generator.getToken(issInjAud, aud, subInjAud, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Different subject for the first signing key")
    /* This test fails as expected because the key used to sign the JWT does not matche the signing key
       for the given subject in the JWT. The DAPS server chooses the key based on the subject of the
       requesting jwt. In this case, no (malicious) client can create JWTs for other subjects */
    fun getDiffSubKey1() {
        print_test("Attempts to get an access_token with another subject for the first client's signing key"); 

        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss2, aud, iss2, context, type, iat, nbf, exp, keyPath, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Different subject for the second signing key")
    /* Check the description from the test before*/
    fun getDiffSubKey2() {
        print_test("Attempts to get an access_token with another subject for the second client's signing key"); 
        
        // Try to get access_token with the given arguments
        val response : String = generator.getToken(iss, aud, sub, context, type, iat, nbf, exp, keyPath2, "RS256");
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("NaN")
    /* Checks if NaN is accepted. It should cause an error when processing NaN */
    fun getNaN() {
        print_test("Requests and verifies an access_token but the algorithm creates a jwt with a NaN field");

        // Try to get access_token with the given arguments
        val response : String = generator.getTokenNaN();
        assertFalse("access_token" in response);
    }

    @Test
    @DisplayName("Nested JSON")
    /* Checks if nested JSON accepted. It should cause an error when processing the JSON because the max_nesting is 100 */
    fun getNested() {
        print_test("Requests and verifies an access_token but the algorithm creates a jwt with a deeply nested JSON");

        // Try to get access_token with the given arguments
        val response : String = generator.getTokenNested();
        assertFalse("access_token" in response);
    }

        @Test
    @DisplayName("MAC confusion attack")
    /* Checks if we can confuse the server by using an asymmetric public key as a symmetrical key for authentication. This
       fails because DAPS only accepys asymmetric algorithms. */
    fun getConfusion() {
        print_test("Requests and verifies an access_token but the algorithm creates a jwt signed with a public asymmetrical aimed at confusing the server and creating a valid jwt.");

        // Try to get access_token with the given arguments
        val response : String = generator.getTokenConfusion(iss, aud, sub, context, type, iat, nbf, exp);
        assertFalse("access_token" in response);
    }
}