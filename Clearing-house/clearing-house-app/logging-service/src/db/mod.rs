use core_lib::constants::{MONGO_ID, MONGO_COLL_PROCESSES, DATABASE_URL, CLEAR_DB, PROCESS_DB, PROCESS_DB_CLIENT, MONGO_COLL_TRANSACTIONS, MONGO_TC};
use core_lib::db::{DataStoreApi, init_database_client};
use core_lib::errors::*;
use core_lib::model::process::Process;
use mongodb::bson::doc;
use mongodb::{Client, Database};
use rocket::fairing::{self, Fairing, Info, Kind};
use rocket::futures::TryStreamExt;
use rocket::{Rocket, Build};
use mongodb::options::{UpdateModifications, FindOneAndUpdateOptions, WriteConcern, CreateCollectionOptions};
use crate::model::TransactionCounter;

#[derive(Clone, Debug)]
pub struct ProcessStoreConfigurator;

#[rocket::async_trait]
impl Fairing for ProcessStoreConfigurator {
    fn info(&self) -> Info {
        Info {
            name: "Configuring Process Database",
            kind: Kind::Ignite
        }
    }
    async fn on_ignite(&self, rocket: Rocket<Build>) -> fairing::Result {
        let db_url: String = rocket.figment().extract_inner(DATABASE_URL).clone().unwrap();
        let clear_db = match rocket.figment().extract_inner(CLEAR_DB){
            Ok(value) => {
                debug!("clear_db: {} found. Preparing to clear database...", &value);
                value
            },
            Err(_) => {
                false
            }
        };
        debug!("Using database url: '{:#?}'", &db_url);

        match init_database_client::<ProcessStore>(&db_url.as_str(), Some(PROCESS_DB_CLIENT.to_string())).await{
            Ok(process_store) => {
                debug!("Check if database is empty...");
                match process_store.client.database(PROCESS_DB)
                    .list_collection_names(None)
                    .await{
                    Ok(colls) => {
                        debug!("... found collections: {:#?}", &colls);
                        if colls.len() > 0 && clear_db{
                            debug!("Database not empty and clear_db == true. Dropping database...");
                            match process_store.client.database(PROCESS_DB).drop(None).await{
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
                            debug!("Create collection {} ...", MONGO_COLL_TRANSACTIONS);
                            match process_store.client.database(PROCESS_DB).create_collection(MONGO_COLL_TRANSACTIONS, options).await{
                                Ok(_) => {
                                    debug!("... done.");
                                }
                                Err(_) => {
                                    debug!("... failed.");
                                    return Err(rocket);
                                }
                            };
                        }
                        debug!("... database initialized.");
                        Ok(rocket.manage(process_store))
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
pub struct ProcessStore {
    client: Client,
    database: Database
}

impl DataStoreApi for ProcessStore {
    fn new(client: Client) -> ProcessStore{
        ProcessStore {
            client: client.clone(),
            database: client.database(PROCESS_DB)
        }
    }
}

impl ProcessStore {
    pub async fn get_transaction_counter(&self) -> Result<Option<i64>>{
        debug!("Getting transaction counter...");
        let coll = self.database.collection::<TransactionCounter>(MONGO_COLL_TRANSACTIONS);
        match coll.find_one(None, None).await?{
            Some(t) => Ok(Some(t.tc)),
            None => Ok(Some(0))
        }
    }

    pub async fn increment_transaction_counter(&self) -> Result<Option<i64>>{
        debug!("Getting transaction counter...");
        let coll = self.database.collection::<TransactionCounter>(MONGO_COLL_TRANSACTIONS);
        let mods = UpdateModifications::Document(doc!{"$inc": {MONGO_TC: 1 }});
        let mut opts = FindOneAndUpdateOptions::default();
        opts.upsert = Some(true);
        match coll.find_one_and_update(doc!{}, mods, opts).await?{
            Some(t) => Ok(Some(t.tc)),
            None => Ok(Some(0))
        }
    }

    pub async fn get_processes(&self) -> Result<Vec<Process>> {
        debug!("Trying to get all processes...");
        let coll = self.database.collection::<Process>(MONGO_COLL_PROCESSES);
        let result = coll.find(None, None).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }

    pub async fn delete_process(&self, pid: &String) -> Result<bool> {
        debug!("Trying to delete process with pid '{}'...", pid);
        let coll = self.database.collection::<Process>(MONGO_COLL_PROCESSES);
        let result = coll.delete_one(doc! { MONGO_ID: pid }, None).await?;
        if result.deleted_count == 1{
            debug!("... deleted one process.");
            Ok(true)
        }
        else{
            warn!("deleted_count={}", result.deleted_count);
            Ok(false)
        }
    }

    /// checks if the id exits
    pub async fn exists_process(&self, pid: &String) -> Result<bool> {
        debug!("Check if process with pid '{}' exists...", pid);
        let coll = self.database.collection::<Process>(MONGO_COLL_PROCESSES);
        let result = coll.find_one(Some(doc! { MONGO_ID: pid }), None).await?;
        match result {
            Some(_r) => {
                debug!("... found.");
                Ok(true)
            },
            None => {
                debug!("Process with pid '{}' does not exist!", pid);
                Ok(false)
            }
        }
    }

    pub async fn get_process(&self, pid: &String) -> Result<Option<Process>> {
        debug!("Trying to get process with id {}...", pid);
        let coll = self.database.collection::<Process>(MONGO_COLL_PROCESSES);
        match coll.find_one(Some(doc!{ MONGO_ID: pid }), None).await{
            Ok(process) => {
                debug!("... found it.");
                Ok(process)
            },
            Err(e) => {
                error!("Error while getting process: {:#?}!", &e);
                Err(Error::from(e))
            }
        }
    }

    pub async fn is_authorized(&self, user: &String, pid: &String) -> Result<bool>{
        debug!("checking if user '{}' is authorized to access '{}'", user, pid);
        return match self.get_process(&pid).await{
            Ok(Some(process)) => {
                let authorized = process.owners.iter().any(|o| {
                    trace!("found owner {}", o);
                    user.eq(o)
                });
                Ok(authorized)
            }
            Ok(None) => {
                trace!("didn't find process");
                Ok(false)
            },
            _ => {
                Err(format!("User '{}' could not be authorized", &user).into())
            }
        }
    }

    // store process in db
    pub async fn store_process(&self, process: Process) -> Result<bool> {
        debug!("Storing process with pid {:#?}...", &process.id);
        let coll = self.database.collection::<Process>(MONGO_COLL_PROCESSES);
        match coll.insert_one(process, None).await {
            Ok(_r) => {
                debug!("...added new process: {}", &_r.inserted_id);
                Ok(true)
            },
            Err(e) => {
                error!("...failed to store process: {:#?}", &e);
                Err(Error::from(e))
            }
        }
    }
}