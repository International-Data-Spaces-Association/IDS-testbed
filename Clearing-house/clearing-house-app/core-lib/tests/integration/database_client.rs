// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests all access the db, so if you run the tests use
// cargo test -- --test-threads=1
// otherwise they will interfere with each other
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
const TEST_CONFIG_FILE: &'static str = "./config.yml";

use core_lib::db::{DataStoreApi, DataStore};
use core_lib::errors::*;
use core_lib::util;

use crate::create_test_enc_document;

fn db_setup() -> DataStore{
    let config = util::load_config(TEST_CONFIG_FILE);

    let db: DataStore = util::configure_db(&config).unwrap();
    if let Err(e) = db.clean_db(){
        panic!("Error while cleaning up database {:?}", e);
    }
    if let Err(e) = db.create_indexes(){
        panic!("Error while setting up database {:?}", e);
    };
    db
}

fn tear_down(db: DataStore){
    if let Err(e) = db.clean_db(){
        panic!("Error while tearing down database {:?}", e);
    }
}

// DOCUMENT
/// Testcase: Document exists in db and is found
#[test]
fn test_document_exists() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_document_exists_pid");
    let dt_id = String::from("test_document_exists_dt");
    let id = String::from("test_document_exists_id");
    let doc = create_test_enc_document(&id, &pid, &dt_id);
    db.add_document(doc.clone())?;

    // run the test
    assert_eq!(db.exists_document(&id)?, true);

    // clean up
    tear_down(db);

    Ok(())
}

/// Testcase: Document does not exist and is not found
#[test]
fn test_document_does_not_exist() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_document_does_not_exist_pid");
    let dt_id = String::from("test_document_does_not_exist_dt");
    let id1 = String::from("test_document_does_not_exist_pid_id1");
    let id2 = String::from("test_document_does_not_exist_pid_id2");
    let doc = create_test_enc_document(&id1, &pid, &dt_id);
    db.add_document(doc.clone())?;

    // run the test
    assert_eq!(db.exists_document(&id2)?, false);

    // clean up
    tear_down(db);

    Ok(())
}

/// Testcase: Document does not exist after delete
#[test]
fn test_delete_document_doc_is_deleted() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_delete_document_doc_is_deleted_pid");
    let dt_id = String::from("test_delete_document_doc_is_deleted_dt");
    let id = String::from("test_delete_document_doc_is_deleted_id");
    let doc = create_test_enc_document(&id, &pid, &dt_id);
    db.add_document(doc.clone())?;

    // db should be able to find the document
    assert_eq!(db.exists_document(&id)?, true);

    // run the test
    assert!(db.delete_document(&id)?);

    // db should not find document anymore
    assert_eq!(db.exists_document(&id)?, false);

    // clean up
    tear_down(db);

    Ok(())
}

/// Testcase: Other Documents still exist after delete
#[test]
fn test_delete_document_check_others() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_delete_document_check_others_pid");
    let dt_id = String::from("test_delete_document_check_others_dt");
    let id1 = String::from("test_delete_document_check_others_id1");
    let id2 = String::from("test_delete_document_check_others_id2");
    let doc1 = create_test_enc_document(&id1, &pid, &dt_id);
    let doc2 = create_test_enc_document(&id2, &pid, &dt_id);
    db.add_document(doc1.clone())?;
    db.add_document(doc2.clone())?;

    // db should be able to find both documents
    assert_eq!(db.exists_document(&id1)?, true);
    assert_eq!(db.exists_document(&id2)?, true);

    // run the test
    assert!(db.delete_document(&id1)?);

    // db should still find the other document
    assert_eq!(db.exists_document(&id2)?, true);

    // clean up
    tear_down(db);

    Ok(())
}

/// Testcase: Document does not exist before delete
#[test]
fn test_delete_document_on_not_existing_doc() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_delete_document_on_not_existing_doc_pid");
    let dt_id = String::from("test_delete_document_on_not_existing_doc_dt");
    let id1 = String::from("test_delete_document_on_not_existing_doc_id1");
    let id2 = String::from("test_delete_document_on_not_existing_doc_id2");
    let doc = create_test_enc_document(&id1, &pid, &dt_id);
    db.add_document(doc.clone())?;

    // run the test
    assert_eq!(db.delete_document(&id2)?, false);

    // clean up
    tear_down(db);

    Ok(())
}

/// Testcase: Find the correct document
#[test]
fn test_get_document() -> Result<()>{
    // empty db and create tables
    let db = db_setup();

    // prepare test data
    let pid = String::from("test_get_document_pid");
    let dt_id = String::from("test_get_document_dt");
    let id1 = String::from("test_get_document_id1");
    let id2 = String::from("test_get_document_id2");
    let doc1 = create_test_enc_document(&id1, &pid, &dt_id);
    let doc2 = create_test_enc_document(&id2, &pid, &dt_id);
    db.add_document(doc1.clone())?;
    db.add_document(doc2.clone())?;

    // db should be able to find both documents
    assert_eq!(db.exists_document(&id1)?, true);
    assert_eq!(db.exists_document(&id2)?, true);

    // the test
    let result = db.get_document(&id1, &pid)?;
    assert_eq!(result.is_some(), true);
    assert_eq!(result.unwrap().id, id1);

    // clean up
    tear_down(db);

    Ok(())
}