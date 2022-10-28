// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// These tests all access the db, so if you run the tests use
// cargo test -- --test-threads=1
// otherwise they will interfere with each other
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
use core_lib::db::DataStoreApi;
use core_lib::errors::*;
use core_lib::model::document::EncryptedDocument;
use mongodb::Client;
use crate::db::DataStore;
use chrono::Utc;

const DATABASE_URL: &'static str = "mongodb://127.0.0.1:27017";

async fn db_setup() -> DataStore {
    let client = Client::with_uri_str(DATABASE_URL).await.unwrap();
    let db = DataStore::new(client);
    db.database.drop(None).await.expect("Database Error");
    db
}

async fn tear_down(db: DataStore){
    db.database.drop(None).await.expect("Database Error");
}

fn create_test_enc_document(id: &String, pid: &String, dt_id: &String) -> EncryptedDocument{
    let mut cts = vec!();
    cts.push(String::from("1::4EBC3F1C2B8CB16C52E41424502FD112015D9C25919C2401514B5DD5B4233B65593CF0A4"));
    cts.push(String::from("2::FE2195305E95B9F931660CBA20B4707A1D92123022371CEDD2E70A538A8771EE7540D9F34845BBAEECEC"));
    let key_ct = String::from("very secure key ct");
    let ts = Utc::now().timestamp();
    EncryptedDocument::new(id.clone(), pid.clone(), dt_id.clone(), ts, 3241, key_ct, cts)
}

// DOCUMENT
/// Testcase: Document exists in db and is found
#[tokio::test]
async fn test_document_exists() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_document_exists_pid");
    let dt_id = String::from("test_document_exists_dt");
    let id = String::from("test_document_exists_id");
    let doc = create_test_enc_document(&id, &pid, &dt_id);
    db.add_document(doc.clone()).await?;

    // run the test
    assert_eq!(db.exists_document(&id).await?, true);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Document does not exist and is not found
#[tokio::test]
async fn test_document_does_not_exist() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_document_does_not_exist_pid");
    let dt_id = String::from("test_document_does_not_exist_dt");
    let id1 = String::from("test_document_does_not_exist_pid_id1");
    let id2 = String::from("test_document_does_not_exist_pid_id2");
    let doc = create_test_enc_document(&id1, &pid, &dt_id);
    db.add_document(doc.clone()).await?;

    // run the test
    assert_eq!(db.exists_document(&id2).await?, false);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Document does not exist after delete
#[tokio::test]
async fn test_delete_document_doc_is_deleted() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_delete_document_doc_is_deleted_pid");
    let dt_id = String::from("test_delete_document_doc_is_deleted_dt");
    let id = String::from("test_delete_document_doc_is_deleted_id");
    let doc = create_test_enc_document(&id, &pid, &dt_id);
    db.add_document(doc.clone()).await?;

    // db should be able to find the document
    assert_eq!(db.exists_document(&id).await?, true);

    // run the test
    assert!(db.delete_document(&id).await?);

    // db should not find document anymore
    assert_eq!(db.exists_document(&id).await?, false);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Other Documents still exist after delete
#[tokio::test]
async fn test_delete_document_check_others() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_delete_document_check_others_pid");
    let dt_id = String::from("test_delete_document_check_others_dt");
    let id1 = String::from("test_delete_document_check_others_id1");
    let id2 = String::from("test_delete_document_check_others_id2");
    let doc1 = create_test_enc_document(&id1, &pid, &dt_id);
    let doc2 = create_test_enc_document(&id2, &pid, &dt_id);
    db.add_document(doc1.clone()).await?;
    db.add_document(doc2.clone()).await?;

    // db should be able to find both documents
    assert_eq!(db.exists_document(&id1).await?, true);
    assert_eq!(db.exists_document(&id2).await?, true);

    // run the test
    assert!(db.delete_document(&id1).await?);

    // db should still find the other document
    assert_eq!(db.exists_document(&id2).await?, true);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Document does not exist before delete
#[tokio::test]
async fn test_delete_document_on_not_existing_doc() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_delete_document_on_not_existing_doc_pid");
    let dt_id = String::from("test_delete_document_on_not_existing_doc_dt");
    let id1 = String::from("test_delete_document_on_not_existing_doc_id1");
    let id2 = String::from("test_delete_document_on_not_existing_doc_id2");
    let doc = create_test_enc_document(&id1, &pid, &dt_id);
    db.add_document(doc.clone()).await?;

    // run the test
    assert_eq!(db.delete_document(&id2).await?, false);

    // clean up
    tear_down(db).await;

    Ok(())
}

/// Testcase: Find the correct document
#[tokio::test]
async fn test_get_document() -> Result<()>{
    // empty db and create tables
    let db = db_setup().await;

    // prepare test data
    let pid = String::from("test_get_document_pid");
    let dt_id = String::from("test_get_document_dt");
    let id1 = String::from("test_get_document_id1");
    let id2 = String::from("test_get_document_id2");
    let doc1 = create_test_enc_document(&id1, &pid, &dt_id);
    let doc2 = create_test_enc_document(&id2, &pid, &dt_id);
    db.add_document(doc1.clone()).await?;
    db.add_document(doc2.clone()).await?;

    // db should be able to find both documents
    assert_eq!(db.exists_document(&id1).await?, true);
    assert_eq!(db.exists_document(&id2).await?, true);

    // the test
    let result = db.get_document(&id1, &pid).await?;
    assert_eq!(result.is_some(), true);
    assert_eq!(result.unwrap().id, id1);

    // clean up
    tear_down(db).await;

    Ok(())
}