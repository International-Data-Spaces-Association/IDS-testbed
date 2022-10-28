use mongodb::{Client, Database, IndexModel};
use mongodb::bson::doc;
use mongodb::options::{CreateCollectionOptions, FindOptions, IndexOptions, WriteConcern};
use rocket::{Build, Rocket};
use rocket::fairing::{self, Fairing, Info, Kind};
use rocket::futures::TryStreamExt;
use rocket::serde::json::json;
use std::convert::TryFrom;

use core_lib::constants::{DATABASE_URL, DOCUMENT_DB, CLEAR_DB, MONGO_COLL_DOCUMENTS, MONGO_DT_ID, MONGO_ID, MONGO_PID, DOCUMENT_DB_CLIENT, MONGO_TC, MONGO_TS};
use core_lib::db::{DataStoreApi, init_database_client};
use core_lib::errors::*;
use core_lib::model::document::{Document, EncryptedDocument};
use core_lib::model::SortingOrder;


#[cfg(test)] mod tests;

#[derive(Clone, Debug)]
pub struct DatastoreConfigurator;

#[rocket::async_trait]
impl Fairing for DatastoreConfigurator {
    fn info(&self) -> Info {
        Info {
            name: "Configuring Document Database",
            kind: Kind::Ignite
        }
    }
    async fn on_ignite(&self, rocket: Rocket<Build>) -> fairing::Result {
        let db_url: String = rocket.figment().extract_inner(DATABASE_URL).clone().unwrap();
        let clear_db = match rocket.figment().extract_inner(CLEAR_DB){
            Ok(value) => {
                debug!("clear_db: '{}' found.", &value);
                value
            },
            Err(_) => {
                false
            }
        };
        debug!("Using mongodb url: '{:#?}'", &db_url);
        match init_database_client::<DataStore>(&db_url.as_str(), Some(DOCUMENT_DB_CLIENT.to_string())).await{
            Ok(datastore) => {
                debug!("Check if database is empty...");
                match datastore.client.database(DOCUMENT_DB)
                    .list_collection_names(None)
                    .await{
                    Ok(colls) => {
                        debug!("... found collections: {:#?}", &colls);
                        if colls.len() > 0 && clear_db{
                            debug!("Database not empty and clear_db == true. Dropping database...");
                            match datastore.client.database(DOCUMENT_DB).drop(None).await{
                                Ok(_) => {
                                    debug!("... done.");
                                }
                                Err(_) => {
                                    debug!("... failed.");
                                    return Err(rocket);
                                }
                            };
                        }
                        if colls.len() == 0 || clear_db{
                            debug!("Database empty. Need to initialize...");
                            let mut write_concern = WriteConcern::default();
                            write_concern.journal = Some(true);
                            let mut options = CreateCollectionOptions::default();
                            options.write_concern = Some(write_concern);
                            debug!("Create collection {} ...", MONGO_COLL_DOCUMENTS);
                            match datastore.client.database(DOCUMENT_DB).create_collection(MONGO_COLL_DOCUMENTS, options).await{
                                Ok(_) => {
                                    debug!("... done.");
                                }
                                Err(_) => {
                                    debug!("... failed.");
                                    return Err(rocket);
                                }
                            };
                            let mut index_options = IndexOptions::default();
                            index_options.unique = Some(true);
                            let mut index_model = IndexModel::default();
                            index_model.keys =  doc!{MONGO_TC: 1};
                            index_model.options = Some(index_options);

                            debug!("Create unique index for {} ...", MONGO_COLL_DOCUMENTS);
                            match datastore.client.database(DOCUMENT_DB).collection::<Document>(MONGO_COLL_DOCUMENTS).create_index(index_model, None).await{
                                Ok(result) => {
                                    debug!("... index {} created", result.index_name);
                                }
                                Err(_) => {
                                    debug!("... failed.");
                                    return Err(rocket);
                                }
                            }
                        }
                        debug!("... database initialized.");
                        Ok(rocket.manage(datastore))
                    }
                    Err(_) => {
                        Err(rocket)
                    }
                }
            },
            Err(_) => Err(rocket)
        }
    }
}

#[derive(Clone)]
pub struct DataStore {
    client: Client,
    database: Database
}

impl DataStoreApi for DataStore {
    fn new(client: Client) -> DataStore{
        DataStore {
            client: client.clone(),
            database: client.database(DOCUMENT_DB)
        }
    }
}


impl DataStore {
    // DOCUMENT
    pub async fn add_document(&self, doc: EncryptedDocument) -> Result<bool> {
        trace!("add_document({:#?})", json!(doc));
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        match coll.insert_one(doc.clone(), None).await {
            Ok(_r) => {
                debug!("added new document: {}", &_r.inserted_id);
                Ok(true)
            },
            Err(e) => {
                error!("failed to store document: {:#?}", &e);
                Err(Error::from(e))
            }
        }
    }

    /// deletes model from db
    pub async fn delete_document(&self, id: &String) -> Result<bool> {
        debug!("Trying to delete entry with id '{}'...", id);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.delete_one(doc! { MONGO_ID: id }, None).await?;
        if result.deleted_count == 1{
            debug!("... deleted one entry.");
            Ok(true)
        }
        else{
            warn!("deleted_count={}", result.deleted_count);
            Ok(false)
        }
    }

    /// checks if the document exists
    /// document ids are globally unique
    pub async fn exists_document(&self, id: &String) -> Result<bool> {
        debug!("Check if document with id '{}' exists...", id);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find_one(Some(doc! { MONGO_ID: id.clone() }), None).await?;
        match result {
            Some(_r) => {
                debug!("... found.");
                Ok(true)
            },
            None => {
                debug!("Document with id '{}' does not exist!", &id);
                Ok(false)
            }
        }
    }

    /// gets the model from the db
    pub async fn get_document(&self, id: &String, pid: &String) -> Result<Option<EncryptedDocument>> {
        debug!("Trying to get doc with id {}...", id);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        match coll.find_one(Some(doc! { MONGO_ID: id.clone(), MONGO_PID: pid.clone()}), None).await{
            Ok(doc) => {
                debug!("... found it.");
                Ok(doc)
            },
            Err(e) => {
                error!("Error while getting document with id {}!", id);
                Err(Error::from(e))
            }
        }
    }

    /// gets documents for a single process from the db
    pub async fn get_documents_for_pid(&self, pid: &String) -> Result<Vec<EncryptedDocument>> {
        debug!("Trying to get all documents for pid {}...", pid);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find(Some(doc! { MONGO_PID: pid.clone() }), None).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }

    /// gets a page of documents for a single process from the db defined by parameters page, size and sort
    pub async fn get_paginated_documents_for_pid(&self, pid: &String, page: u64, size: u64, sort: &SortingOrder) -> Result<Vec<EncryptedDocument>> {
        debug!("...trying to get page {} of size {} of documents for pid {}...", pid, page, size);
        let mut options = FindOptions::default();
        options.skip = Some((page - 1) * size);
        options.limit = Some(i64::try_from(size)?);
        options.sort = match sort{
            SortingOrder::Ascending => {
                Some(doc!{MONGO_TS: 1})
            },
            SortingOrder::Descending => {
                Some(doc!{MONGO_TS: -1})
            }
        };
        options.allow_disk_use = Some(true);

        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find(Some(doc! { MONGO_PID: pid.clone() }), options).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        debug!("found {:#?}", &result);
        Ok(result)
    }


    /// gets documents for a single process from the db
    pub async fn get_document_with_previous_tc(&self, tc: i64) -> Result<Option<EncryptedDocument>> {
        let previous_tc = tc - 1;
        debug!("Trying to get document for tc {} ...", previous_tc);
        if previous_tc < 0 {
            info!("... not entry exists.");
            Ok(None)
        }
        else{
            let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
            match coll.find_one(Some(doc! {MONGO_TC: previous_tc}), None).await{
                Ok(doc) => {
                    debug!("... found it.");
                    Ok(doc)
                },
                Err(e) => {
                    error!("Error while getting latest document!");
                    Err(Error::from(e))
                }
            }
        }
    }

    /// gets documents of a specific document type for a single process from the db
    pub async fn get_documents_of_dt_for_pid(&self, dt_id: &String, pid: &String) -> Result<Vec<EncryptedDocument>> {
        debug!("...trying to get all documents for pid {} of dt {}...", pid, dt_id);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find(Some(doc! { MONGO_PID: pid.clone(), MONGO_DT_ID: dt_id.clone() }), None).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }

    /// gets a page of documents of a specific document type for a single process from the db defined by parameters page, size and sort
    pub async fn get_paginated_documents_of_dt_for_pid(&self, dt_id: &String, pid: &String, page: u64, size: u64, sort: &SortingOrder) -> Result<Vec<EncryptedDocument>> {
        debug!("...trying to get page {} of size {} of documents for pid {} of dt {}...", pid, dt_id, page, size);
        let mut options = FindOptions::default();
        options.skip = Some((page - 1) * size);
        options.limit = Some(i64::try_from(size)?);
        options.sort = match sort{
            SortingOrder::Ascending => {
                Some(doc!{MONGO_TS: 1})
            },
            SortingOrder::Descending => {
                Some(doc!{MONGO_TS: -1})
            }
        };
        options.allow_disk_use = Some(true);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find(Some(doc! { MONGO_PID: pid.clone(), MONGO_DT_ID: dt_id.clone() }), options).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }

    /// counts documents for a single process from the db
    pub async fn count_documents_for_pid(&self, pid: &String) -> Result<u64> {
        debug!("...counting all documents for pid {}...", pid);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.count_documents(Some(doc! { MONGO_PID: pid.clone() }), None).await?;
        Ok(result)
    }

    /// counts documents of a specific document type for a single process from the db
    pub async fn count_documents_of_dt_for_pid(&self, dt_id: &String, pid: &String) -> Result<u64> {
        debug!("Trying to get all documents for pid {} of dt {}...", pid, dt_id);
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.count_documents(Some(doc! { MONGO_PID: pid.clone(), MONGO_DT_ID: dt_id.clone() }), None).await?;
        Ok(result)
    }

    /// gets all documents from the db
    pub async fn get_all_documents(&self) -> Result<Vec<EncryptedDocument>> {
        debug!("Trying to get all documents...");
        let coll = self.database.collection::<EncryptedDocument>(MONGO_COLL_DOCUMENTS);
        let result = coll.find(None, None).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }
}
