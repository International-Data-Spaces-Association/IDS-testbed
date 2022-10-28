use core_lib::constants::{MONGO_COLL_MASTER_KEY, KEYRING_DB, FILE_DEFAULT_DOC_TYPE, DATABASE_URL, CLEAR_DB, KEYRING_DB_CLIENT};
use core_lib::db::{DataStoreApi, init_database_client};
use core_lib::errors::*;
use core_lib::util::read_file;
use mongodb::{Client, Database};
use rocket::fairing::{self, Fairing, Info, Kind};
use rocket::futures::TryStreamExt;
use rocket::{Rocket, Build};
use std::process::exit;

use crate::model::crypto::MasterKey;
use crate::model::doc_type::DocumentType;


pub(crate) mod doc_type;
#[cfg(test)] mod tests;

#[derive(Clone, Debug)]
pub struct KeyStore {
    client: Client,
    database: Database
}

impl DataStoreApi for KeyStore {
    fn new(client: Client) -> KeyStore{
        KeyStore {
            client: client.clone(),
            database: client.database(KEYRING_DB)
        }
    }
}

#[derive(Clone, Debug)]
pub struct KeyringDbConfigurator;

#[rocket::async_trait]
impl Fairing for KeyringDbConfigurator {
    fn info(&self) -> Info {
        Info {
            name: "Configuring Keyring Database",
            kind: Kind::Ignite
        }
    }
    async fn on_ignite(&self, rocket: Rocket<Build>) -> fairing::Result {
        let db_url: String = rocket.figment().extract_inner(DATABASE_URL).clone().unwrap();
        let clear_db = match rocket.figment().extract_inner(CLEAR_DB) {
            Ok(value) => {
                debug!("clear_db: '{}' found.", &value);
                value
            },
            Err(_) => {
                false
            }
        };
        debug!("Using database url: '{:#?}'", &db_url);

        match init_database_client::<KeyStore>(&db_url.as_str(), Some(KEYRING_DB_CLIENT.to_string())).await {
            Ok(keystore) => {
                debug!("Check if database is empty...");
                match keystore.client.database(KEYRING_DB)
                    .list_collection_names(None)
                    .await {
                    Ok(colls) => {
                        debug!("... found collections: {:#?}", &colls);
                        if colls.len() > 0 && clear_db {
                            debug!("Database not empty and clear_db == true. Dropping database...");
                            match keystore.client.database(KEYRING_DB).drop(None).await {
                                Ok(_) => {
                                    debug!("... done.");
                                }
                                Err(_) => {
                                    debug!("... failed.");
                                    return Err(rocket);
                                }
                            };
                        }
                        if colls.len() == 0 || clear_db {
                            debug!("Database empty. Need to initialize...");
                            debug!("Adding initial document type...");
                            match serde_json::from_str::<DocumentType>(&read_file(FILE_DEFAULT_DOC_TYPE).unwrap_or(String::new())) {
                                Ok(dt) => {
                                    match keystore.add_document_type(dt).await {
                                        Ok(_) => {
                                            debug!("... done.");
                                        },
                                        Err(e) => {
                                            error!("Error while adding initial document type: {:#?}", e);
                                            return Err(rocket);
                                        }
                                    }
                                }
                                _ => {
                                    error!("Error while loading initial document type");
                                    return Err(rocket);
                                }
                            };
                            debug!("Creating master key...");
                            // create master key
                            match keystore.store_master_key(MasterKey::new_random()).await {
                                Ok(true) => {
                                    debug!("... done.");
                                },
                                _ => {
                                    error!("... failed to create master key");
                                    return Err(rocket);
                                }
                            };
                        }
                        debug!("... database initialized.");
                        Ok(rocket.manage(keystore))
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

impl KeyStore {

    /// Only one master key may exist in the database.
   pub async fn store_master_key(&self, key: MasterKey) -> Result<bool>{
        debug!("Storing new master key...");
        let coll = self.database.collection::<MasterKey>(MONGO_COLL_MASTER_KEY);
        debug!("... but first check if there's already one.");
        let result= coll.find(None, None).await
            .expect("Error retrieving the master keys")
            .try_collect().await.unwrap_or_else(|_| vec![]);

        if result.len() > 1{
            error!("Master Key table corrupted!");
            exit(1);
        }
        if result.len() == 1{
            error!("Master key already exists!");
            Ok(false)
        }
        else{
            //let db_key = bson::to_bson(&key)
            //    .expect("failed to serialize master key for database");
            match coll.insert_one(key, None).await{
                Ok(_r) => {
                    Ok(true)
                },
                Err(e) => {
                    error!("master key could not be stored: {:?}", &e);
                    panic!("master key could not be stored")
                }
            }
        }
    }

    /// Only one master key may exist in the database.
    pub async fn get_msk(&self) -> Result<MasterKey> {
        let coll = self.database.collection::<MasterKey>(MONGO_COLL_MASTER_KEY);
        let result= coll.find(None, None).await
            .expect("Error retrieving the master keys")
            .try_collect().await.unwrap_or_else(|_| vec![]);

        if result.len() > 1{
            error!("Master Key table corrupted!");
            exit(1);
        }
        if result.len() == 1{
            Ok(result[0].clone())
        }
        else {
            error!("Master Key missing!");
            exit(1);
        }
    }
}