use biscuit::jwa::SignatureAlgorithm;
use biscuit::jwk::JWKSet;
use biscuit::{CompactJson, Empty};
use core_lib::api::ApiClient;
use core_lib::api::auth::{self, ApiKey};
use core_lib::api::client::daps_api::DapsApiClient;
use core_lib::constants::DAPS_API_URL;
use core_lib::errors::*;
use core_lib::util;
use serde::{Deserialize, Serialize};
use crate::{TOKEN, TEST_CONFIG};

#[derive(Debug, PartialEq, Clone, Serialize, Deserialize)]
struct CustomClaims {
    /// Recipient for which the JWT is intended
    scopes: Vec<String>,
    #[serde(rename = "securityProfile")]
    security_profile: String,
    #[serde(rename = "@type")]
    claim_type: String,
    #[serde(rename = "@context")]
    claim_context: String,
    #[serde(rename = "transportCertsSha256")]
    transport_certs_sha256: String,
}
impl CompactJson for CustomClaims {
}

#[test]
fn test_valid_claims() -> Result<()>{
    // configure daps_api
    let api_url = util::load_from_test_config(DAPS_API_URL, TEST_CONFIG);
    let daps_api = DapsApiClient::new(&api_url);
    // convert "default" key to HashMap
    let jwks = daps_api.get_jwks()?;
    let jwt: Result<ApiKey<CustomClaims, Empty>> = auth::validate_token(TOKEN, &jwks, Some(SignatureAlgorithm::RS256));
    assert!(jwt.is_ok(), "Token is invalid. Update test token!");
    let claims = jwt.unwrap().claims();
    assert_eq!(claims.private.scopes, vec!["idsc:IDS_CONNECTOR_ATTRIBUTES_ALL".to_string()]);
    assert_eq!(claims.private.security_profile, "idsc:TRUST_SECURITY_PROFILE".to_string());
    assert_eq!(claims.private.transport_certs_sha256, "c15e6558088dbfef215a43d2507bbd124f44fb8facd561c14561a2c1a669d0e0".to_string());
    Ok(())
}

#[test]
fn test_invalid_claims() -> Result<()>{
    let invalid_token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJpZHMtYXR0cmlidXRlcyI6eyJzZWN1cml0eV9wcm9maWxlIjp7ImF1ZGl0X2xvZ2dpbmciOjJ9fSwiaWRzX21lbWJlcnNoaXAiOnRydWUsImlkcy11cmkiOiJodHRwOi8vc29tZS11cmkiLCJ0cmFuc3BvcnRDZXJ0c1NoYTI1NiI6ImJhY2I4Nzk1NzU3MzBiYjA4M2YyODNmZDViNjdhOGNiODk2OTQ0ZDFiZTI4YzdiMzIxMTdjZmM3NTdjODFlOTYiLCJzY29wZXMiOlsiaWRzX2Nvbm5lY3RvciJdLCJhdWQiOiJJRFNfQ29ubmVjdG9yIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJzdWIiOiJDPURFLE89RnJhdW5ob2ZlcixPVT1BSVNFQyxDTj02ZTYxNThiNC02OWZmLTRkMDQtYTg0Yi1hNDI4NTY0YWU0ZTYiLCJuYmYiOjE1NjI5MjU1MDAsImV4cCI6MTU2MjkyOTEwMH0.V4GZq3ZFnFAULoCiwhXtpno1uLab-mmAwRchhb2w_k4v0VYQYgWsFGf1EJPX-0QJfz4_WtTS_nQMq-MG9fP-Pe9BVXY43Wb9UBrrlaxylwnYbV0BCgUc-T-0uWdtJkRoQDqySnNRzYDMOKxZcOTXLG5d4eOHUulgiHa2muUeWw_c7bV-DKzNxUCzinxCEEVaOpovArJhRHSGgLd-8UI6BA-xehNQu_lmcaQ2ut0_VT-njwkY98haowrvEVcN9yHTm2jrWv-ajrs9phiR24A4wUqPMysDYZzIq_F6RfUBWovuu534nfo5mBXlc1JpT2NydN_dE2FM9nAWPpJ6_BEZxg";
    // configure daps_api
    let api_url = util::load_from_test_config(DAPS_API_URL, TEST_CONFIG);
    let daps_api = DapsApiClient::new(&api_url);
    // convert "default" key to HashMap
    let jwks:JWKSet<Empty> = daps_api.get_jwks()?;
    let jwt: Result<ApiKey<CustomClaims, Empty>> = auth::validate_token(invalid_token, &jwks, Some(SignatureAlgorithm::RS256));
    assert!(jwt.is_err(), "Token is valid. this should not happen, really!");
    Ok(())
}
