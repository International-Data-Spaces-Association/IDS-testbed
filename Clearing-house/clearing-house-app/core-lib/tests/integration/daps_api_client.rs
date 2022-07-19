// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests are integration tests and need an up-and-running keyring-api
// Use config.yml to configure the urls correctly.
// Before running the tests make sure that there's a valid token in auth/mod.rs
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
use core_lib::api::ApiClient;
use core_lib::api::client::daps_api::DapsApiClient;
use core_lib::constants::{DAPS_API_URL, DAPS_KID};
use core_lib::errors::*;
use core_lib::util;
use biscuit::jwk::{JWK, KeyType};
use biscuit::Empty;
use crate::TEST_CONFIG;

/// before running make sure the blockchain api is available
#[test]
fn test_get_jwks() -> Result<()>{
    // configure daps_api
    let api_url = util::load_from_test_config(DAPS_API_URL, TEST_CONFIG);
    let daps_api = DapsApiClient::new(&api_url);
    // convert "default" key to HashMap

    let jwk: JWK<Empty> = daps_api.get_jwks().unwrap().find(DAPS_KID).unwrap().clone();
    assert_eq!(KeyType::RSA, jwk.algorithm.key_type());
    assert_eq!(DAPS_KID, jwk.common.key_id.unwrap());
    Ok(())
}