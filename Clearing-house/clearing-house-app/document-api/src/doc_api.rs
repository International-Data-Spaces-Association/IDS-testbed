use biscuit::Empty;
use rocket::State;
use chrono::Local;
use core_lib::{
    api::{
        ApiResponse,
        auth::ApiKey,
        claims::IdsClaims,
        client::keyring_api::KeyringApiClient,
        DocumentReceipt
    },
    constants::ROCKET_DOC_API,
    model::{
        crypto::{KeyCt, KeyCtList},
        document::Document
    }
};
use rocket::fairing::AdHoc;
use rocket::serde::json::{json, Json};
use std::convert::TryFrom;
use crate::db::DataStore;
use core_lib::constants::{DEFAULT_NUM_RESPONSE_ENTRIES, PAYLOAD_PART};
use core_lib::model::SortingOrder;
use core_lib::model::SortingOrder::Ascending;

#[post("/", format = "json", data = "<document>")]
async fn create_enc_document(
    api_key: ApiKey<IdsClaims, Empty>,
    db: &State<DataStore>,
    key_api: &State<KeyringApiClient>,
    document: Json<Document>
) -> ApiResponse {
    trace!("user '{:?}' with claims {:?}", api_key.sub(), api_key.claims());
    let doc: Document = document.into_inner();
    trace!("requested document is: '{:#?}'", json!(doc));

    // data validation
    let payload: Vec<String> = doc.parts.iter()
        .filter(|p| String::from(PAYLOAD_PART) == p.name)
        .map(|p| p.content.as_ref().unwrap().clone()).collect();
    if payload.len() > 1 {
        return ApiResponse::BadRequest(String::from("Document contains two payloads!"));
    }
    else if payload.len() == 0 {
        return ApiResponse::BadRequest(String::from("Document contains no payload!"));
    }

    // check if doc id already exists
    match db.exists_document(&doc.id).await {
        Ok(true) => {
            warn!("Document exists already!");
            ApiResponse::BadRequest(String::from("Document exists already!"))
        },
        _ => {
            debug!("Document does not exists!");

            //TODO: get keys to encrypt document
            debug!("getting keys");
            let keys;
            match key_api.generate_keys(&api_key.raw(), &doc.pid, &doc.dt_id) {
                Ok(key_map) => {
                    keys = key_map;
                    debug!("got keys");
                },
                Err(e) => {
                    error!("Error while retrieving keys: {:?}", e);
                    return ApiResponse::InternalError(String::from("Error while retrieving keys!"))
                },
            };

            debug!("start encryption");
            let mut enc_doc;
            match doc.encrypt(keys) {
                Ok(ct) => {
                    debug!("got ct");
                    enc_doc = ct
                },
                Err(e) => {
                    error!("Error while encrypting: {:?}", e);
                    return ApiResponse::InternalError(String::from("Error while encrypting!"))
                },
            };

            // chain the document to previous documents
            debug!("add the chain hash...");
            // get the document with the previous tc
            match db.get_document_with_previous_tc(doc.tc).await{
                Ok(Some(previous_doc)) => {
                    enc_doc.hash = previous_doc.hash();
                },
                Ok(None) => {
                    if doc.tc == 0{
                        info!("No entries found for pid {}. Beginning new chain!", &doc.pid);
                    }
                    else{
                        // If this happens, db didn't find a tc entry that should exist.
                        return ApiResponse::InternalError(String::from("Error while creating the chain hash!"))
                    }
                },
                Err(e) => {
                    error!("Error while creating the chain hash: {:?}", e);
                    return ApiResponse::InternalError(String::from("Error while creating the chain hash!"))
                }
            }

            // prepare the success result message


            let receipt = DocumentReceipt::new(enc_doc.ts, &enc_doc.pid, &enc_doc.id, &enc_doc.hash);

            debug!("storing document ....");
            // store document
            //TODO store encrypted keys
            match db.add_document(enc_doc).await {
                Ok(_b) => ApiResponse::SuccessCreate(json!(receipt)),
                Err(e) => {
                    error!("Error while adding: {:?}", e);
                    ApiResponse::InternalError(String::from("Error while storing document!"))
                }
            }
        }
    }
}

#[delete("/<pid>/<id>", format = "json")]
async fn delete_document(api_key: ApiKey<IdsClaims, Empty>, db: &State<DataStore>, pid: String, id: String) -> ApiResponse {
    debug!("delete called...");
    trace!("user '{:?}' with claims {:?}", api_key.sub(), api_key.claims());
    // this is only a sanity check, i.e. we make sure id/pid pair exists
    match db.get_document(&id, &pid).await{
        Ok(Some(_enc_doc)) => {
            match db.delete_document(&id).await{
                Ok(true) => ApiResponse::SuccessNoContent(String::from("Document deleted!")),
                Ok(false) => ApiResponse::NotFound(String::from("Document does not exist!")),
                Err(e) => {
                    error!("Error while deleting document: {:?}", e);
                    ApiResponse::InternalError(format!("Error while deleting document {}!", &id))
                }
            }
        }
        _ => {
            warn!("Document '{}' with pid '{}' not found!", &id, &pid);
            ApiResponse::NotFound(String::from("Document to delete not found"))
        }
    }
}

#[get("/<pid>?<doc_type>&<page>&<size>&<sort>", format = "json")]
async fn get_enc_documents_for_pid(
    api_key: ApiKey<IdsClaims, Empty>,
    key_api: &State<KeyringApiClient>,
    db: &State<DataStore>,
    doc_type: Option<String>,
    page: Option<i32>,
    size: Option<i32>,
    sort: Option<SortingOrder>,
    pid: String) -> ApiResponse {
    debug!("Trying to retrieve documents for pid '{}'...", &pid);
    trace!("...user '{:?}' with claims {:?}", api_key.sub(), api_key.claims());
    debug!("...page: {:#?}, size:{:#?} and sort:{:#?}", page, size, sort);

    let mut using_pagination = false;

    // parameter sanitization pagination
    let mut sanitized_page = match page{
        Some(p) => {
            using_pagination = true;
            if p > 0{
                u64::try_from(p).unwrap()
            }
            else{
                warn!("...invalid page requested. Falling back to 1.");
                1
            }
        },
        None => 1
    };

    let sanitized_size = match size{
        Some(s) => {
            using_pagination = true;
            if s > 0{
                u64::try_from(s).unwrap()
            }
            else{
                warn!("...invalid size requested. Falling back to default.");
                DEFAULT_NUM_RESPONSE_ENTRIES
            }
        },
        None => DEFAULT_NUM_RESPONSE_ENTRIES
    };

    let sanitized_sort = match sort{
        Some(s) => {
            using_pagination = true;
            s
        },
        None => Ascending
    };

    // either call db with type filter or without to get cts
    let cts;
    let start = Local::now();
    if doc_type.is_some(){
        debug!("... applying type filter");
        if using_pagination{
            debug!("... using pagination with page: {}, size:{} and sort:{:#?}", sanitized_page, sanitized_size, &sanitized_sort);
            // using the number of docs in db we check that the given page number is valid or limit it
            match db.count_documents_of_dt_for_pid(doc_type.as_ref().unwrap(), &pid).await{
                Ok(number_of_docs) => {
                    // rounded up number of pages
                    let number_of_pages = (number_of_docs + sanitized_size - 1) / sanitized_size;
                    if sanitized_page > number_of_pages {
                        warn!("...invalid page requested. Falling back to {}.", number_of_pages);
                        sanitized_page = number_of_pages;
                    }
                }
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
            // getting the docs for specified page and size
            debug!("...but only of document type: '{}'", doc_type.as_ref().unwrap());
            match db.get_paginated_documents_of_dt_for_pid(doc_type.as_ref().unwrap(), &pid, sanitized_page, sanitized_size, &sanitized_sort).await{
                Ok(cts_type_filter) => cts = cts_type_filter,
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
        }
        else{
            debug!("...but only of document type: '{}'", doc_type.as_ref().unwrap());
            match db.get_documents_of_dt_for_pid(doc_type.as_ref().unwrap(), &pid).await{
                Ok(cts_type_filter) => cts = cts_type_filter,
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
        }
    }
    else{
        debug!("...no type filter applied");
        if using_pagination{
            debug!("...using pagination with page: {}, size:{} and sort:{:#?}", sanitized_page, sanitized_size, &sanitized_sort);
            // using the number of docs in db we check that the given page number is valid or limit it
            match db.count_documents_for_pid(&pid).await{
                Ok(number_of_docs) => {
                    // rounded up number of pages
                    let number_of_pages = (number_of_docs + sanitized_size - 1) / sanitized_size;
                    if sanitized_page > number_of_pages {
                        if number_of_pages == 0 {
                            sanitized_page = 1;
                        }
                        else{
                            sanitized_page = number_of_pages;
                        }
                        warn!("...invalid page requested. Falling back to {}.", sanitized_page);
                    }
                }
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
            match db.get_paginated_documents_for_pid(&pid, sanitized_page, sanitized_size, &sanitized_sort).await{
                Ok(cts_type_filter) => cts = cts_type_filter,
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
        }
        else {
            match db.get_documents_for_pid(&pid).await {
                //TODO: would like to send "{}" instead of "null" when dt is not found
                Ok(cts_unfiltered) => cts = cts_unfiltered,
                Err(e) => {
                    error!("Error while retrieving document: {:?}", e);
                    return ApiResponse::InternalError(format!("Error while retrieving document for {}", &pid))
                }
            }
        }
    };
    // The db might contain no documents in which case we get an empty vector
    if cts.is_empty(){
        debug!("Queried empty pid: {}", &pid);
        ApiResponse::SuccessOk(json!(cts))
    }
    else{
        // Documents found for pid, now decrypting them
        debug!("Found {} documents. Getting keys from keyring...", cts.len());
        let key_cts: Vec<KeyCt> = cts.iter()
            .map(|e| KeyCt::new(e.id.clone(), e.keys_ct.clone())).collect();
        // caution! we currently only support a single dt per call, so we use the first dt we found
        let key_cts_list = KeyCtList::new(cts[0].dt_id.clone(), key_cts);
        // decrypt cts
        let key_maps = match key_api.decrypt_multiple_keys(&api_key.raw(), &pid,&key_cts_list){
            Ok(key_map) => {
                key_map
            }
            Err(e) => {
                error!("Error while retrieving keys from keyring: {:?}", e);
                return ApiResponse::InternalError(format!("Error while retrieving keys from keyring"))
            }
        };
        debug!("... keys received. Starting decryption...");
        let pts_bulk : Vec<Document> = cts.iter().zip(key_maps.iter())
            .filter_map(|(ct,key_map)|{
                if ct.id != key_map.id{
                    error!("Document and map don't match");
                };
                match ct.decrypt(key_map.map.keys.clone()){
                    Ok(d) => Some(d),
                    Err(e) => {
                        warn!("Got empty document from decryption! {:?}", e);
                        None
                    }
                }
            }).collect();
        debug!("...done.");
        let end = Local::now();
        let diff = end - start;
        info!("Total time taken to run in ms: {}", diff.num_milliseconds());
        ApiResponse::SuccessOk(json!(pts_bulk))
    }
}

/// Retrieve document with id for process with pid
#[get("/<pid>/<id>?<hash>", format = "json")]
async fn get_enc_document(api_key: ApiKey<IdsClaims, Empty>, key_api: &State<KeyringApiClient>, db: &State<DataStore>, pid: String, id: String, hash: Option<String>) -> ApiResponse {
    trace!("user '{:?}' with claims {:?}", api_key.sub(), api_key.claims());
    trace!("trying to retrieve document with id '{}' for pid '{}'", &id, &pid);
    if hash.is_some(){
        debug!("integrity check with hash: {}", hash.as_ref().unwrap());
    }

    match db.get_document(&id, &pid).await{
        //TODO: would like to send "{}" instead of "null" when dt is not found
        Ok(Some(ct)) => {
            match hex::decode(&ct.keys_ct){
                Ok(key_ct) => {
                    match key_api.decrypt_keys(&api_key.raw(), &pid, &ct.dt_id, &key_ct){
                        Ok(key_map) => {
                            //TODO check the hash
                            match ct.decrypt(key_map.keys){
                                Ok(d) => ApiResponse::SuccessOk(json!(d)),
                                Err(e) => {
                                    warn!("Got empty document from decryption! {:?}", e);
                                    return ApiResponse::NotFound(format!("Document {} not found!", &id))
                                }
                            }
                        }
                        Err(e) => {
                            error!("Error while retrieving keys from keyring: {:?}", e);
                            return ApiResponse::InternalError(format!("Error while retrieving keys"))
                        }
                    }

                },
                Err(e) => {
                    error!("Error while decoding ciphertext: {:?}", e);
                    return ApiResponse::InternalError(format!("Key Ciphertext corrupted"))
                }
            }
        },
        Ok(None) => {
            debug!("Nothing found in db!");
            return ApiResponse::NotFound(format!("Document {} not found!", &id))
        }
        Err(e) => {
            error!("Error while retrieving document: {:?}", e);
            return ApiResponse::InternalError(format!("Error while retrieving document {}", &id))
        }
    }
}

pub fn mount_api() -> AdHoc {
    AdHoc::on_ignite("Mounting Document API", |rocket| async {
        rocket
            .mount(ROCKET_DOC_API, routes![create_enc_document, delete_document,
                                            get_enc_document, get_enc_documents_for_pid])
    })
}