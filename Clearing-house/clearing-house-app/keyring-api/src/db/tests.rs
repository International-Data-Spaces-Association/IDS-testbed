// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests all access the db, so if you run the tests use
// cargo test -- --test-threads=1
// otherwise they will interfere with each other
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
use core_lib::errors::*;
use mongodb::Client;

use crate::db::{DataStoreApi, KeyStore};
use crate::model::doc_type::DocumentType;

const DATABASE_URL: &'static str = "mongodb://127.0.0.1:27018";

async fn db_setup() -> KeyStore {
    let client = Client::with_uri_str(DATABASE_URL).await.unwrap();
    let db = KeyStore::new(client);
    db.database.drop(None).await.expect("Database Error");
    db
}

async fn tear_down(db: KeyStore){
    db.database.drop(None).await.expect("Database Error");
}

/// Testcase: Document type exists
#[tokio::test]
async fn test_document_type_exists() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let dt = DocumentType::new(String::from("test_document_type_exists_dt_dt"), String::from("test_document_type_exists_dt_pid"), vec!());
    db.add_document_type(dt.clone()).await?;

    // run the test: db should find document type
    assert_eq!(db.exists_document_type(&dt.pid, &dt.id).await?, true);

    // clean up
    tear_down(db).await;

    Ok(())
}


/// Testcase: Document type exists for other pid and is not found
#[tokio::test]
async fn test_document_type_exists_for_other_pid() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let dt = DocumentType::new(String::from("test_document_type_exists_for_other_pid_dt"), String::from("test_document_type_exists_for_other_pid_pid"), vec!());
    let wrong_pid = String::from("the_wrong_pid");
    db.add_document_type(dt.clone()).await?;

    // run the test: db should not find the document type
    assert_eq!(db.exists_document_type(&wrong_pid, &dt.id).await?, false);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Delete on document type with correct pid results in deletion of document type
#[tokio::test]
async fn test_delete_document_type_correct_pid() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data and insert into db
    let dt = DocumentType::new(String::from("test_delete_document_type_correct_pid_id"), String::from("test_delete_document_type_correct_pid_pid"), vec!());
    let dt2 = DocumentType::new(String::from("test_delete_document_type_correct_pid_id"), String::from("test_delete_document_type_correct_pid_pid_2"), vec!());
    db.add_document_type(dt.clone()).await?;
    db.add_document_type(dt2.clone()).await?;

    // run the test
    db.delete_document_type(&dt.id, &dt.pid).await?;

    // db should not find document type
    assert_eq!(db.exists_document_type(&dt.pid, &dt.id).await?, false);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Delete on document type with wrong pid results not in the deletion of document type
#[tokio::test]
async fn test_delete_document_type_wrong_pid() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data and insert into db
    let dt = DocumentType::new(String::from("test_delete_document_type_correct_pid_id"), String::from("test_delete_document_type_correct_pid_pid"), vec!());
    let wrong_pid = String::from("the_wrong_pid");
    db.add_document_type(dt.clone()).await?;

    // run the test
    db.delete_document_type(&dt.id, &wrong_pid).await?;

    // db should still find document type
    assert_eq!(db.exists_document_type(&dt.pid, &dt.id).await?, true);

    // clean up
    tear_down(db).await;

    Ok(())
}