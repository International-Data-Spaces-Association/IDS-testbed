// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests are integration tests and need an up-and-running keyring-api and
// document-api. Use config.yml to configure the urls correctly.
// Before running the tests make sure that there's a valid token in auth/mod.rs
// Also note: Clean up will not work if a test fails.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
use core_lib::api::ApiClient;
use core_lib::constants::DOCUMENT_API_URL;
use core_lib::util;
use core_lib::errors::*;
use core_lib::api::client::document_api::DocumentApiClient;
use crate::{TOKEN, create_test_document, delete_test_doc_type_from_keyring, insert_test_doc_type_into_keyring, TEST_CONFIG};

/// Testcase: Standard case: store document as first document for pid
#[test]
fn test_store_first_document() -> Result<()> {
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_store_first_document_dt");
    let pid = String::from("test_store_first_document_pid");
    let expected_doc = create_test_document(&pid, &dt_id, 0);
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    // run the test
    let result = doc_api.create_document(&TOKEN.to_string(), &expected_doc)?;
    assert_eq!(result.chain_hash, String::from("0"));

    // clean up
    assert!(doc_api.delete_document(&TOKEN.to_string(), &expected_doc.pid, &expected_doc.id)?);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}

/// Testcase: Standard case: store document as first document for pid
#[test]
fn test_store_chained_document() -> Result<()> {
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_store_chained_document_dt");
    let pid = String::from("test_store_chained_document_pid");
    let first_doc = create_test_document(&pid, &dt_id, 0);
    let second_doc = create_test_document(&pid, &dt_id, 1);
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    // create test data in db
    doc_api.create_document(&TOKEN.to_string(), &first_doc)?;

    // run the test
    let result = doc_api.create_document(&TOKEN.to_string(), &second_doc)?;
    assert_ne!(result.chain_hash, String::from("0"));

    // clean up
    assert!(doc_api.delete_document(&TOKEN.to_string(), &first_doc.pid, &first_doc.id)?);
    assert!(doc_api.delete_document(&TOKEN.to_string(), &second_doc.pid, &second_doc.id)?);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}

/// Testcase: Standard case: retrieve document.
#[test]
fn test_get_document() -> Result<()>{
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_get_document_type_1");
    let pid = String::from("test_get_document_process_1");
    let expected_doc = create_test_document(&pid, &dt_id, 0);
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    // create test data in db
    doc_api.create_document(&TOKEN.to_string(), &expected_doc)?;

    // run test
    let result = doc_api.get_document(&TOKEN.to_string(), &pid, &expected_doc.id)?.unwrap();
    println!("Result: {:?}", result);

    // checks
    // ids should match
    assert_eq!(result.id, expected_doc.id);

    // same document type
    assert_eq!(result.dt_id, expected_doc.dt_id);

    // checking the parts
    for i in 0..result.parts.len()-1{
        assert_eq!(expected_doc.parts[i].name, result.parts[i].name);
        assert_eq!(expected_doc.parts[i].content, result.parts[i].content);
    }

    // clean up
    assert!(doc_api.delete_document(&TOKEN.to_string(), &expected_doc.pid, &expected_doc.id)?);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}

/// Testcase: Retrieve all documents for pid, but there are no documents
#[test]
fn test_get_no_documents_for_pid() -> Result<()>{
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_get_no_documents_for_pid_type");
    let pid_with_doc = String::from("test_get_no_documents_for_pid_pid_1");
    let pid_without_doc = String::from("test_get_no_documents_for_pid_pid_2");
    let expected_doc = create_test_document(&pid_with_doc, &dt_id, 0);
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid_with_doc, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid_with_doc, &dt_id)?;

    // create test data in db
    doc_api.create_document(&TOKEN.to_string(), &expected_doc)?;

    // run test
    let result = doc_api.get_documents_for_pid(&TOKEN.to_string(), &pid_without_doc)?;
    println!("Result: {:?}", result);

    // check that there are no documents found
    assert_eq!(result.len(), 0);

    // clean up
    assert!(doc_api.delete_document(&TOKEN.to_string(), &expected_doc.pid, &expected_doc.id)?);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid_with_doc, &dt_id)?;

    Ok(())
}

/// Testcase: Standard case: Retrieve all documents for pid
//TODO
#[test]
fn test_get_documents_for_pid() -> Result<()>{
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_get_documents_for_pid_type");
    let pid = String::from("test_get_documents_for_pid_pid");
    let doc1 = create_test_document(&pid, &dt_id, 0);
    let doc2 = create_test_document(&pid, &dt_id, 1);
    let doc3 = create_test_document(&pid, &dt_id, 2);
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    // create test data in db
    doc_api.create_document(&TOKEN.to_string(), &doc1)?;
    doc_api.create_document(&TOKEN.to_string(), &doc2)?;
    doc_api.create_document(&TOKEN.to_string(), &doc3)?;

    // run test
    let result = doc_api.get_documents_for_pid(&TOKEN.to_string(), &pid)?;
    println!("Result: {:?}", result);

    // check that we got three documents back
    assert_eq!(result.len(), 3);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    assert!(doc_api.delete_document(&TOKEN.to_string(), &pid, &doc1.id)?);
    assert!(doc_api.delete_document(&TOKEN.to_string(), &pid, &doc2.id)?);
    assert!(doc_api.delete_document(&TOKEN.to_string(), &pid, &doc3.id)?);


    Ok(())
}

/// Testcase: Ensure that IDS ids can be used if they are url_encoded
#[test]
fn test_create_document_url_encoded_id() -> Result<()>{
    // configure client_api
    let api_url = util::load_from_test_config(DOCUMENT_API_URL, TEST_CONFIG);
    let doc_api = DocumentApiClient::new(&api_url);

    // prepare test data
    let dt_id = String::from("test_create_document_url_encoded_id_type_3");
    let pid = String::from("test_create_document_url_encoded_id_process_3");
    let id = String::from("https://w3id.org/idsa/autogen/ResultMessage/71ad9d3a-3743-4966-afa3-f5b02ba91eaa");
    // clean up doc type (in case of previous test failure)
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;
    insert_test_doc_type_into_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    let mut doc = create_test_document(&pid, &dt_id, 0);
    doc.id = id.clone();

    // run test
    let hash = doc_api.create_document(&TOKEN.to_string(), &doc);

    // check that it's not an error
    assert!(hash.is_ok());

    // clean up
    assert!(doc_api.delete_document(&TOKEN.to_string(), &doc.pid, &id)?);

    // tear down
    delete_test_doc_type_from_keyring(&TOKEN.to_string(), &pid, &dt_id)?;

    Ok(())
}