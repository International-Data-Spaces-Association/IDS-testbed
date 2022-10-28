use biscuit::Empty;
use core_lib::{
    api::{
        ApiResponse,
        auth::ApiKey,
        client::document_api::DocumentApiClient,
        claims::IdsClaims,
        crypto::get_jwks,
    },
    constants::{DEFAULT_NUM_RESPONSE_ENTRIES, MAX_NUM_RESPONSE_ENTRIES, DEFAULT_PROCESS_ID},
    model::{
        document::Document,
        process::Process,
        SortingOrder,
        SortingOrder::Ascending
    }
};
use rocket::serde::json::{json, Json};
use rocket::fairing::AdHoc;
use rocket::form::validate::Contains;
use rocket::State;
use std::convert::TryFrom;

use crate::model::{ids::{
    message::IdsMessage,
    request::ClearingHouseMessage,
}, OwnerList, DataTransaction};
use crate::db::ProcessStore;
use crate::model::constants::{ROCKET_CLEARING_HOUSE_BASE_API, ROCKET_LOG_API, ROCKET_QUERY_API, ROCKET_PROCESS_API, ROCKET_PK_API};


#[post( "/<pid>", format = "json", data = "<message>")]
async fn log(
    apikey: ApiKey<IdsClaims, Empty>,
    db: &State<ProcessStore>,
    doc_api: &State<DocumentApiClient>,
    key_path: &State<String>,
    message: Json<ClearingHouseMessage>,
    pid: String
) -> ApiResponse {
    // Add non-InfoModel information to IdsMessage
    let msg = message.into_inner();
    let mut m = msg.header;
    m.payload = msg.payload;
    m.payload_type = msg.payload_type;
    m.pid = Some(pid.clone());

    // validate that there is a payload
    if m.payload.is_none() || (m.payload.is_some() && m.payload.as_ref().unwrap().trim().is_empty()){
        error!("Trying to log an empty payload!");
        return ApiResponse::BadRequest(String::from("No payload received for logging!"))
    }

    // get credentials for user [this should not happen, the connector should make sure of it]
    let user;
    match getConnectorIdentifier(&apikey){
        None => {
            // cannot authenticate user without credentials
            return ApiResponse::Unauthorized(String::from("Invalid user!"));
        },
        Some(u) => user = u
    };

    // filter out calls for default process id and call application logic
    match DEFAULT_PROCESS_ID.eq(pid.as_str()){
        true => {
            warn!("Log to default pid '{}' not allowed", DEFAULT_PROCESS_ID);
            ApiResponse::BadRequest(String::from("Document already exists"))
        },
        false => {
            // convenience: if process does not exist, we create it but only if no error occurred before
            match db.get_process(&pid).await {
                Ok(Some(_p)) => {
                    debug!("Requested pid '{}' exists. Nothing to create.", &pid);
                }
                Ok(None) => {
                    info!("Requested pid '{}' does not exist. Creating...", &pid);
                    // create a new process
                    let new_process = Process::new(pid.clone(), vec!(user.clone()));

                    if db.store_process(new_process).await.is_err() {
                        error!("Error while creating process '{}'", &pid);
                        return ApiResponse::InternalError(String::from("Error while creating process"))
                    }
                }
                Err(_) => {
                    error!("Error while getting process '{}'", &pid);
                    return ApiResponse::InternalError(String::from("Error while getting process"))
                }
            }

            // now check if user is authorized to write to pid
            match db.is_authorized(&user, &pid).await {
                Ok(true) => info!("User authorized."),
                Ok(false) => {
                    warn!("User is not authorized to write to pid '{}'", &pid);
                    warn!("This is the forbidden branch");
                    return ApiResponse::Forbidden(String::from("User not authorized!"))
                }
                Err(_) => {
                    error!("Error while checking authorization of user '{}' for '{}'", &user, &pid);
                    return ApiResponse::InternalError(String::from("Error during authorization"))
                }
            }

            debug!("logging message for pid {}", &pid);
            log_message(apikey, db, user, doc_api, key_path.inner().as_str(), m.clone()).await
        }
    }
}

#[post( "/<pid>", format = "json", data = "<message>")]
async fn create_process(
    apikey: ApiKey<IdsClaims, Empty>,
    db: &State<ProcessStore>,
    message: Json<ClearingHouseMessage>,
    pid: String
) -> ApiResponse {
    let msg = message.into_inner();
    let mut m = msg.header;
    m.payload = msg.payload;
    m.payload_type = msg.payload_type;

    // get credentials for user [this should not happen, the connector should make sure of it]
    let user;
    match getConnectorIdentifier(&apikey){
        None => {
            // cannot authenticate user without credentials
            return ApiResponse::Unauthorized(String::from("Invalid user!"))
        },
        Some(u) => user = u
    };

    // validate payload
    let mut owners = vec!(user.clone());
    let payload = m.payload.clone().unwrap_or(String::new());
    if !payload.is_empty() {
        trace!("OwnerList: '{:#?}'", &payload);
        match serde_json::from_str::<OwnerList>(&payload){
            Ok(owner_list) => {
                for o in owner_list.owners{
                    if !owners.contains(&o){
                        owners.push(o);
                    }
                }
            },
            Err(e) => {
                error!("Error while creating process '{}': {}", &pid, e);
                return ApiResponse::BadRequest(String::from("Invalid owner list!"))
            }
        };
    };

    // check if the pid already exists
    match db.get_process(&pid).await{
        Ok(Some(p)) => {
            warn!("Requested pid '{}' already exists.", &p.id);
            if !p.owners.contains(user) {
                ApiResponse::Forbidden(String::from("User not authorized!"))
            }
            else {
                ApiResponse::BadRequest(String::from("Process already exists!"))
            }
        }
        _ => {
            // filter out calls for default process id
            match DEFAULT_PROCESS_ID.eq(pid.as_str()) {
                true => {
                    warn!("Log to default pid '{}' not allowed", DEFAULT_PROCESS_ID);
                    ApiResponse::BadRequest(String::from("Document already exists"))
                },
                false => {
                    info!("Requested pid '{}' will have {} owners", &pid, owners.len());

                    // create process
                    info!("Requested pid '{}' does not exist. Creating...", &pid);
                    let new_process = Process::new(pid.clone(), owners);

                    match db.store_process(new_process).await{
                        Ok(_) => {
                            ApiResponse::SuccessCreate(json!(pid.clone()))
                        }
                        Err(e) => {
                            error!("Error while creating process '{}': {}", &pid, e);
                            ApiResponse::InternalError(String::from("Error while creating process"))
                        }
                    }
                }
            }
        }
    }
}

async fn log_message(
    apikey: ApiKey<IdsClaims, Empty>,
    db: &State<ProcessStore>,
    user: String,
    doc_api: &State<DocumentApiClient>,
    key_path: &str,
    message: IdsMessage
) -> ApiResponse {
    debug!("transforming message to document...");
    let payload = message.payload.as_ref().unwrap().clone();
    // transform message to document
    let mut doc = Document::from(message);
    match db.get_transaction_counter().await{
        Ok(Some(tid)) => {
            debug!("Storing document...");
            doc.tc = tid;
            return match doc_api.create_document(&apikey.raw, &doc){
                Ok(doc_receipt) => {
                    debug!("Increase transabtion counter");
                    match db.increment_transaction_counter().await{
                        Ok(Some(_tid)) => {
                            debug!("Creating receipt...");
                            let transaction = DataTransaction{
                                transaction_id: doc.get_formatted_tc(),
                                timestamp: doc_receipt.timestamp,
                                process_id: doc_receipt.pid,
                                document_id: doc_receipt.doc_id,
                                payload,
                                chain_hash: doc_receipt.chain_hash,
                                client_id: user,
                                clearing_house_version: env!("CARGO_PKG_VERSION").to_string(),
                            };
                            debug!("...done. Signing receipt...");
                            ApiResponse::SuccessCreate(json!(transaction.sign(key_path)))
                        }
                        _ => {
                            error!("Error while incrementing transaction id!");
                            ApiResponse::InternalError(String::from("Internal error while preparing transaction data"))
                        }
                    }

                },
                Err(e) => {
                    error!("Error while creating document: {:?}", e);
                    ApiResponse::BadRequest(String::from("Document already exists"))
                }
            }
        },
        _ => {
            error!("Error while getting transaction id!");
            ApiResponse::InternalError(String::from("Internal error while preparing transaction data"))
        }
    }
}

#[post("/<_pid>", format = "json", rank=50)]
async fn unauth(_pid: Option<String>) -> ApiResponse {
    ApiResponse::Unauthorized(String::from("Token not valid!"))
}

#[post("/<_pid>/<_id>", format = "json", rank=50)]
async fn unauth_id(_pid: Option<String>, _id: Option<String>) -> ApiResponse {
    ApiResponse::Unauthorized(String::from("Token not valid!"))
}

#[post("/<pid>?<page>&<size>&<sort>", format = "json", data = "<message>")]
async fn query_pid(
    apikey: ApiKey<IdsClaims, Empty>,
    db: &State<ProcessStore>,
    page: Option<i32>,
    size: Option<i32>,
    sort: Option<SortingOrder>,
    doc_api: &State<DocumentApiClient>,
    pid: String,
    message: Json<ClearingHouseMessage>
) -> ApiResponse {
    debug!("page: {:#?}, size:{:#?} and sort:{:#?}", page, size, sort);

    // get credentials for user [this should not happen, the connector should make sure of it]
    let user;
    match getConnectorIdentifier(&apikey){
        None => {
            // cannot authenticate user without credentials
            return ApiResponse::Unauthorized(String::from("Invalid user!"))
        },
        Some(u) => user = u
    };

    // check if process exists
    match db.exists_process(&pid).await {
        Ok(true) => info!("User authorized."),
        Ok(false) => return ApiResponse::NotFound(String::from("Process does not exist!")),
        Err(e) => {
            error!("Error while checking process '{}' for user '{}'", &pid, &user);
            return ApiResponse::InternalError(String::from("Cannot authorize user!"))
        }
    };

    // now check if user is authorized to read infos in pid
    match db.is_authorized(&user, &pid).await {
        Ok(true) => {
            info!("User authorized.");
        },
        Ok(false) => {
            warn!("User is not authorized to write to pid '{}'", &pid);
            return ApiResponse::Forbidden(String::from("User not authorized!"))
        }
        Err(_) => {
            error!("Error while checking authorization of user '{}' for '{}'", &user, &pid);
            return ApiResponse::InternalError(String::from("Cannot authorize user!"))
        }
    }

    // sanity check for pagination
    let sanitized_page = match page {
        Some(p) => {
            if p >= 0 {
                p
            } else {
                warn!("...invalid page requested. Falling back to 0.");
                1
            }
        },
        None => 1
    };

    let sanitized_size = match size {
        Some(s) => {
            let converted_max = i32::try_from(MAX_NUM_RESPONSE_ENTRIES).unwrap();
            if s > converted_max {
                warn!("...invalid size requested. Falling back to default.");
                converted_max
            } else {
                if s > 0 {
                    s
                } else {
                    warn!("...invalid size requested. Falling back to default.");
                    i32::try_from(DEFAULT_NUM_RESPONSE_ENTRIES).unwrap()
                }
            }
        },
        None => i32::try_from(DEFAULT_NUM_RESPONSE_ENTRIES).unwrap()
    };

    let sanitized_sort = match sort {
        Some(s) => s,
        None => Ascending
    };

    match doc_api.get_documents_for_pid_paginated(&apikey.raw, &pid, sanitized_page, sanitized_size, sanitized_sort) {
        Ok(docs) => {
            let messages: Vec<IdsMessage> = docs.iter().map(|d| IdsMessage::from(d.clone())).collect();
            ApiResponse::SuccessOk(json!(messages))
        },
        Err(e) => {
            error!("Error while retrieving message: {:?}", e);
            ApiResponse::InternalError(format!("Error while retrieving messages for pid {}!", &pid))
        }
    }
}

#[post("/<pid>/<id>", format = "json", data = "<message>")]
async fn query_id(apikey: ApiKey<IdsClaims, Empty>, db: &State<ProcessStore>, doc_api: &State<DocumentApiClient>, pid: String, id: String, message: Json<ClearingHouseMessage>) -> ApiResponse {

    // get credentials for user [this should not happen, the connector should make sure of it]
    let user;
    match getConnectorIdentifier(&apikey){
        None => {
            // cannot authenticate user without credentials
            return ApiResponse::Unauthorized(String::from("Invalid user!"))
        },
        Some(u) => user = u
    };

    // check if process exists
    match db.exists_process(&pid).await {
        Ok(true) => info!("User authorized."),
        Ok(false) => return ApiResponse::NotFound(String::from("Process does not exist!")),
        Err(e) => {
            error!("Error while checking process '{}' for user '{}'", &pid, &user);
            return ApiResponse::InternalError(String::from("Cannot authorize user!"))
        }
    };

    // now check if user is authorized to read infos in pid
    match db.is_authorized(&user, &pid).await {
        Ok(true) => {
            info!("User authorized.");
        },
        Ok(false) => {
            warn!("User is not authorized to write to pid '{}'", &pid);
            return ApiResponse::Forbidden(String::from("User not authorized!"))
        }
        Err(_) => {
            error!("Error while checking authorization of user '{}' for '{}'", &user, &pid);
            return ApiResponse::InternalError(String::from("Cannot authorize user!"))
        }
    }

    match doc_api.get_document(&apikey.raw, &pid, &id) {
        Ok(Some(doc)) => {
            // transform document to IDS message
            let queried_message = IdsMessage::from(doc);
            ApiResponse::SuccessOk(json!(queried_message))
        },
        Ok(None) => {
            debug!("Queried a non-existing document: {}", &id);
            ApiResponse::NotFound(format!("No message found with id {}!", &id))
        },
        Err(e) => {
            error!("Error while retrieving message: {:?}", e);
            ApiResponse::InternalError(format!("Error while retrieving message with id {}!", &id))
        }
    }
}

#[get("/.well-known/jwks.json", format = "json")]
async fn get_public_sign_key(key_path: &State<String>) -> ApiResponse {
    match get_jwks(key_path.as_str()){
        Some(jwks) => ApiResponse::SuccessOk(json!(jwks)),
        None => ApiResponse::InternalError(String::from("Error reading signing key"))
    }
}

pub fn mount_api() -> AdHoc {
    AdHoc::on_ignite("Mounting Clearing House API", |rocket| async {
        rocket
            .mount(format!("{}{}", ROCKET_CLEARING_HOUSE_BASE_API, ROCKET_LOG_API).as_str(), routes![log, unauth])
            .mount(format!("{}", ROCKET_PROCESS_API).as_str(), routes![create_process, unauth])
            .mount(format!("{}{}", ROCKET_CLEARING_HOUSE_BASE_API, ROCKET_QUERY_API).as_str(),
                   routes![query_id, query_pid, unauth, unauth_id])
            .mount(format!("{}", ROCKET_PK_API).as_str(), routes![get_public_sign_key])
    })
}

fn getConnectorIdentifier(apikey: &ApiKey<IdsClaims, Empty>) -> Option<String> {
    match apikey.sub() {
        Some(subject) => Some(subject),
        None => {
            // No credentials, ergo no authorization possible
            error!("Cannot authorize user. Missing credentials");
            None
        }
    }
}