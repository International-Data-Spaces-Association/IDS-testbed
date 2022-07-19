// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests are integration tests and need an up-and-running keyring-api
// Use config.yml to configure the urls correctly.
// Before running the tests make sure that there's a valid token in auth/mod.rs
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
use core_lib::api::ApiClient;
use core_lib::api::client::keyring_api::KeyringApiClient;
use core_lib::constants::KEYRING_API_URL;
use core_lib::errors::*;
use core_lib::util;
use crate::{TOKEN, delete_test_doc_type_from_keyring, insert_test_doc_type_into_keyring, TEST_CONFIG};

/// The tests in this module requires a running key-ring-api
/// Testcase: Generate keys for test document type and check if the key_map is plausible
#[test]
fn test_generate_keys() -> Result<()> {
    // configure client_api
    let api_url = util::load_from_test_config(KEYRING_API_URL, TEST_CONFIG);
    let key_api = KeyringApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_dt");
    let pid = String::from("test_pid");
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    // get the keys from keyring api
    let keys = key_api.generate_keys(&TOKEN.to_string(), &pid, &dt_id)?;

    println!("key_ct: {}", hex::encode_upper(keys.keys_enc.as_ref().unwrap()));

    // check that KeyMap is meant for encryption
    assert_eq!(keys.enc, true);

    // check that there's a key_ct
    assert!(keys.keys_enc.is_some());

    // check that there are three keys (one for each part in the dt)
    assert_eq!(keys.keys.keys().len(), 3);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}

/// Testcase: Decrypt keys and check that they match the previously generated keys
#[test]
fn test_decrypt_keys() -> Result<()> {
    // configure client_api
    let api_url = util::load_from_test_config(KEYRING_API_URL, TEST_CONFIG);
    let key_api = KeyringApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_dt");
    let pid = String::from("test_pid");
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    // generate keys from keyring api
    let keys = key_api.generate_keys(&TOKEN.to_string(), &pid, &dt_id)?;

    // decrypt the keys
    let dec_keys = key_api.decrypt_keys(&TOKEN.to_string(), &pid, &dt_id, keys.keys_enc.as_ref().unwrap())?;

    // check that KeyMap is meant for decryption
    assert_eq!(dec_keys.enc, false);

    // check that there's no key_ct
    assert!(dec_keys.keys_enc.is_none());

    // check that the keys match the previously generated ones
    keys.keys.values().for_each( |entry| {
            let dec_entry = dec_keys.keys.get(&entry.id).unwrap();
            assert_eq!(entry.key, dec_entry.key);
            assert_eq!(entry.nonce, dec_entry.nonce);
            assert_eq!(entry.id, dec_entry.id);
        }
    );

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}