import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.PrivateKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.JwtConsumer;
import com.nimbusds.jose.jwk.JWKSet;
import java.net.URL; 
import java.security.PublicKey;
import java.security.Key;
import java.io.StringWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

class TokenGenerator {

    val daps_url : String = File("test_config.txt").readLines().get(12).split("=")[1];
    //TODO retrieve it from https://daps-dev.aisec.fraunhofer.de/.well-known/oauth-authorization-server/v3 

    fun getPrivKey(pemFile : String, alg : String) : PrivateKey{
        val privateKeyString : String = File(pemFile).readText(Charsets.UTF_8);
        val keyString : String = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "")
                                                 .replace(System.lineSeparator(),"")
                                                 .replace("-----END PRIVATE KEY-----", "");
        val decoder : Decoder = Base64.getDecoder();
        val decoded : ByteArray = decoder.decode(keyString);
        var keyFactory : KeyFactory; 
        if(alg.equals("ES256") || alg.equals("ES512")){
            keyFactory = KeyFactory.getInstance("EC");
        }else{
            keyFactory = KeyFactory.getInstance("RSA");
        }
        val keySpec : PKCS8EncodedKeySpec = PKCS8EncodedKeySpec(decoded);
        val privKey : PrivateKey = keyFactory.generatePrivate(keySpec);
        return privKey;
    }
 
    fun getToken(iss : String, aud : String, sub : String, context : String, type : String,
                iat : Long, nbf : Long, exp : Long, pemFile : String, alg : String): String {
        //Get private key from pem file in PKCS8 format
        var privKey : PrivateKey = getPrivKey(pemFile, alg);
        

        try {
            //Set claims for jwt
            val claims : HashMap<String, Any?> = HashMap<String, Any?>();
            claims.put("iss", iss);
            claims.put("aud", aud);
            claims.put("sub", sub);
            claims.put("@context", context);
            claims.put("@type", type);
            claims.put("iat", iat);
            claims.put("exp", exp);
            claims.put("nbf", nbf);

            //Use library to build jwt
            var jwt : String;
            when(alg){
                "RS256" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.RS256, privKey).compact();
                "RS512" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.RS512, privKey).compact();
                "PS256" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.PS256, privKey).compact();
                "PS512" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.PS512, privKey).compact();
                "ES256" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.ES256, privKey).compact();
                "ES512" -> jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.ES512, privKey).compact();
                else -> jwt = "";

            }
            println("JWT: "+jwt);
            
            //Build request string
            val req : String = "grant_type=client_credentials&"+
                "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"+
                "client_assertion="+jwt+"&"+
                "scope=idsc:IDS_CONNECTOR_ATTRIBUTES_ALL";
            println("\nRequesting access_token from DAPS\n");

            //Send request to daps server and get response
            val client : HttpClient = HttpClient.newBuilder().build();
            val request : HttpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(daps_url+"token"))
                                        .POST(HttpRequest.BodyPublishers.ofString(req))
                                        .build();
            val response : HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString());
            println("Response: "+response.body())
            println("Code: "+response.statusCode()+"\n")
            return response.body();
        }
        catch(ex : Exception) {
            println(ex.message);
        }
        
        return "Error";
    }

    fun getTokenAlgNone(iss : String, aud : String, sub : String, context : String, type : String,
    iat : Long, nbf : Long, exp : Long): String {
        try {
            //Set claims for jwt
            val claims : HashMap<String, Any?> = HashMap<String, Any?>();
            claims.put("iss", iss);
            claims.put("aud", aud);
            claims.put("sub", sub);
            claims.put("@context", context);
            claims.put("@type", type);
            claims.put("iat", iat);
            claims.put("exp", exp);
            claims.put("nbf", nbf);

            //Use library to build jwt
            val jwt : String = Jwts.builder().setClaims(claims).compact();
            println("JWT: "+jwt);

            //Build request string
            val req : String = "grant_type=client_credentials&"+
                "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"+
                "client_assertion="+jwt+"&"+
                "scope=idsc:IDS_CONNECTOR_ATTRIBUTES_ALL";
            println("\nRequesting access_token from DAPS\n");

            //Send request to daps server and get response
            val client : HttpClient = HttpClient.newBuilder().build();
            val request : HttpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(daps_url+"token"))
                                        .POST(HttpRequest.BodyPublishers.ofString(req))
                                        .build();
            val response : HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString());
            println("Response: "+response.body())
            println("Code: "+response.statusCode()+"\n")
            return response.body();
        }
        catch(ex : Exception) {
            println(ex.message);
        }

        return "Error";
    }
 
fun getTokenNaN(): String {
        try {
            //Create payload
            val header : String = "eyJhbGciOiJub25lIn0"; // alg = none
            // {"value":NaN}
            val bytes : ByteArray= byteArrayOf(123, 34, 118, 97, 108, 117, 101, 34, 58, 78, 97, 78, 125);
            val encoder : Encoder = Base64.getEncoder().withoutPadding();
            val body : String = encoder.encodeToString(bytes);
            val jwt = header+"."+body;
            println("JWT: "+jwt);

            //Build request string
            val req : String = "grant_type=client_credentials&"+
                "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"+
                "client_assertion="+jwt+"&"+
                "scope=idsc:IDS_CONNECTOR_ATTRIBUTES_ALL";
            println("\nRequesting access_token from DAPS\n");

            //Send request to daps server and get response
            val client : HttpClient = HttpClient.newBuilder().build();
            val request : HttpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(daps_url+"token"))
                                        .POST(HttpRequest.BodyPublishers.ofString(req))
                                        .build();
            val response : HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString());
            println("Response: "+response.body())
            println("Code: "+response.statusCode()+"\n")
            return response.body();
        }
        catch(ex : Exception) {
            println(ex.message);
        }

        return "Error";
    }

    fun getTokenNested(): String {
        try {            
            // Create big nested jwt of form '{"a":' * 1000
            val header : String = "eyJhbGciOiJub25lIn0"; // alg = none
            val bytes : ByteArray = byteArrayOf(123, 34, 97, 34, 58);
            // {"a":
            var mil : ByteArray = byteArrayOf();
            for(i in 1..1000){
                mil += bytes;
            }
            val encoder : Encoder = Base64.getEncoder().withoutPadding();
            val body : String = encoder.encodeToString(mil);

            //Use library to build jwt
            val jwt = header+"."+body;
            println("JWT: "+jwt);

            //Build request string
            val req : String = "grant_type=client_credentials&"+
                "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"+
                "client_assertion="+jwt+"&"+
                "scope=idsc:IDS_CONNECTOR_ATTRIBUTES_ALL";
            println("\nRequesting access_token from DAPS\n");

            //Send request to daps server and get response
            val client : HttpClient = HttpClient.newBuilder().build();
            val request : HttpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(daps_url+"token"))
                                        .POST(HttpRequest.BodyPublishers.ofString(req))
                                        .build();
            val response : HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString());
            println("Response: "+response.body())
            println("Code: "+response.statusCode()+"\n")
            return response.body();
        }
        catch(ex : Exception) {
            println(ex.message);
        }

        return "Error";
    }

    fun getTokenConfusion(iss : String, aud : String, sub : String, context : String, type : String,
                iat : Long, nbf : Long, exp : Long): String {
        
        //Retrieve public key from the server and use it as a private key to sign
        // the jwt with the hs256 alg
        //TODO retrieve url from https://daps-dev.aisec.fraunhofer.de/.well-known/oauth-authorization-server/v3
        val publicKeys : JWKSet = JWKSet.load(URL(daps_url+".well-known/jwks.json"));
        val pubKey : PublicKey = publicKeys.getKeyByKeyId("default").toRSAKey().toPublicKey();

        //Transform public key into pem file and then load it as a hmac key
        val stringWriter : StringWriter = StringWriter();
        val pemWriter : JcaPEMWriter = JcaPEMWriter(stringWriter);
        pemWriter.writeObject(pubKey);
        pemWriter.close();
        
        try {
            //Set claims for jwt (jose4j)
            val claims : JwtClaims = JwtClaims();
            claims.setClaim("iss", iss);
            claims.setClaim("aud", aud);
            claims.setClaim("sub", sub);
            claims.setClaim("@context", context);
            claims.setClaim("@type", type);
            claims.setClaim("iat", iat);
            claims.setClaim("exp", exp);
            claims.setClaim("nbf", nbf);

            //Use library to build jwt, signed with HS256 to try and confuse the server
            val jws : JsonWebSignature = JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setKey(pubKey);
            jws.setDoKeyValidation(false);
            val jwt : String = jws.getCompactSerialization();
            println("JWT: "+jwt);
            
            //Build request string
            val req : String = "grant_type=client_credentials&"+
                "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"+
                "client_assertion="+jwt+"&"+
                "scope=idsc:IDS_CONNECTOR_ATTRIBUTES_ALL";
            println("\nRequesting access_token from DAPS\n");

            //Send request to daps server and get response
            val client : HttpClient = HttpClient.newBuilder().build();
            val request : HttpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(daps_url+"token"))
                                        .POST(HttpRequest.BodyPublishers.ofString(req))
                                        .build();
            val response : HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString());
            println("Response: "+response.body())
            println("Code: "+response.statusCode()+"\n")
            return response.body();
        }
        catch(ex : Exception) {
            println(ex.message);
        }
        
        return "Error";
    }

    fun verifyTokenRequest(responseBody : String, iss : String, aud : String, sub : String,
                           context : String, type : String, secProf : String,
                           connector : String, scope : String, transCert : String): Boolean{

        //Retrieve access_token from the http response
        val json : JSONObject = JSONObject(responseBody);
        val access_token : String = json.getString("access_token");
        println("Verifying access_token\n");

        // Get JWKS from well-known keys stored in the DAPS
        val httpsJwks : HttpsJwks = HttpsJwks(daps_url+".well-known/jwks.json");

        // Create new JWKS key resolver, selects JWK based on key ID in JWT header
        val jwksKeyResolver : HttpsJwksVerificationKeyResolver = HttpsJwksVerificationKeyResolver(httpsJwks);

        // Create validation requirements according to the DAPS specification
        val jwtConsumer = JwtConsumerBuilder()
            .setExpectedAudience(true, aud)
            .setExpectedIssuer(iss)
            .setAllowedClockSkewInSeconds(30) // If machines are not synchronized it could lead to errors so we allow 30 seconds of difference
            .setRequireNotBefore() 
            .setRequireIssuedAt()
            .setRequireJwtId()
            .setRequireExpirationTime()
            .setRequireSubject()
            .setVerificationKeyResolver(jwksKeyResolver) // Get decryption key from JWKS
            .build();

        try {
            val claims : JwtClaims = jwtConsumer.processToClaims(access_token);
            //Process claims accordingly
            if (!claims.getClaimValue("@type").equals(type)){
                throw Exception("\nFailed @type verification\nGot @type: "
                                +claims.getClaimValue("@type").toString()+"\n");
            }
            if (!claims.getClaimValue("@context").equals(context)){
                throw Exception("\nFailed @context verification\nGot @context: "
                                +claims.getClaimValue("@context").toString()+"\n");
            }
            if (!claims.getSubject().equals(sub)){
                throw Exception("\nFailed subject verification\nGot subject: "
                                +claims.getSubject()+"\n");
            }
            if (!claims.getClaimValue("securityProfile").equals(secProf)){
                throw Exception("\nFailed securityProfile verification\nGot securityProfile: "
                                +claims.getClaimValue("securityProfile").toString()+"\n");
            }
            if (!claims.getClaimValue("referringConnector").equals(connector)){
                throw Exception("\nFailed referringConnector verification\nGot referringConnector: "
                                +claims.getClaimValue("referringConnector").toString()+"\n");
            }
            if (!claims.getClaimValue("transportCertsSha256").equals(transCert)){
                throw Exception("\nFailed transportCertsSha256 verification\nGot transportCertsSha256: "
                                +claims.getClaimValue("transportCertsSha256").toString()+"\n");
            }
            /*if (!claims.getClaimValue("scope").equals(scope)){
                throw Exception("\nFailed scope verification\nGot scope: "
                                +claims.getClaimValue("scope").toString()+"\n");
            }*/
            val runtimeScope : Any = claims.getClaimValue("scopes");
            if(runtimeScope is ArrayList<*>){
                if(!runtimeScope.contains(scope)){
                    throw Exception("\nFailed scopes verification\nGot scopes: "
                                +claims.getClaimValue("scopes").toString()+"\n");
                }
            }else{
                throw Exception("\nFailed scopes verification, scopes is not an ArrayList\nGot scopes: "
                                +claims.getClaimValue("referringConnector").toString()+"\n");
            }


            println("Verification successful\n");
            return true;
        } catch (ex : Exception) {
            println(ex.message);
        }

        return false;
    }
}