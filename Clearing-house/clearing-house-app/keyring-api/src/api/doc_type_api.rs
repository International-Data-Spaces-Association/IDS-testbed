use core_lib::api::ApiResponse;
use core_lib::constants::{ROCKET_DOC_TYPE_API, DEFAULT_PROCESS_ID};
use rocket::fairing::AdHoc;
use rocket::State;
use rocket::serde::json::{json,Json};

use crate::db::KeyStore;
use crate::model::doc_type::DocumentType;

#[post("/", format = "json", data = "<doc_type>")]
async fn create_doc_type(db: &State<KeyStore>, doc_type: Json<DocumentType>) -> ApiResponse {
    let doc_type: DocumentType = doc_type.into_inner();
    debug!("adding doctype: {:?}", &doc_type);
    match db.exists_document_type(&doc_type.pid, &doc_type.id).await{
        Ok(true) => ApiResponse::BadRequest(String::from("doctype already exists!")),
        Ok(false) => {
            match db.add_document_type(doc_type.clone()).await{
                Ok(()) => ApiResponse::SuccessCreate(json!(doc_type)),
                Err(e) => {
                    error!("Error while adding doctype: {:?}", e);
                    return ApiResponse::InternalError(String::from("Error while adding document type!"))
                }
            }
        },
        Err(e) => {
            error!("Error while adding document type: {:?}", e);
            return ApiResponse::InternalError(String::from("Error while checking database!"))
        }
    }
}

#[post("/<id>", format = "json", data = "<doc_type>")]
async fn update_doc_type(db: &State<KeyStore>, id: String, doc_type: Json<DocumentType>) -> ApiResponse {
    let doc_type: DocumentType = doc_type.into_inner();
    match db.exists_document_type(&doc_type.pid, &doc_type.id).await{
        Ok(true) => ApiResponse::BadRequest(String::from("Doctype already exists!")),
        Ok(false) => {
            match db.update_document_type(doc_type, &id).await{
                Ok(id) => ApiResponse::SuccessOk(json!(id)),
                Err(e) => {
                    error!("Error while adding doctype: {:?}", e);
                    return ApiResponse::InternalError(String::from("Error while storing document type!"))
                }
            }
        },
        Err(e) => {
            error!("Error while adding document type: {:?}", e);
            return ApiResponse::InternalError(String::from("Error while checking database!"))
        }
    }
}

#[delete("/<id>", format = "json")]
async fn delete_default_doc_type(db: &State<KeyStore>, id: String) -> ApiResponse{
   delete_doc_type(db, id, DEFAULT_PROCESS_ID.to_string()).await
}

#[delete("/<pid>/<id>", format = "json")]
async fn delete_doc_type(db: &State<KeyStore>, id: String, pid: String) -> ApiResponse{
    match db.delete_document_type(&id, &pid).await{
        Ok(true) => ApiResponse::SuccessNoContent(String::from("Document type deleted!")),
        Ok(false) => ApiResponse::NotFound(String::from("Document type does not exist!")),
        Err(e) => {
            error!("Error while deleting doctype: {:?}", e);
            ApiResponse::InternalError(format!("Error while deleting document type with id {}!", id))
        }
    }
}

#[get("/<id>", format = "json")]
async fn get_default_doc_type(db: &State<KeyStore>, id: String) -> ApiResponse {
    get_doc_type(db, id, DEFAULT_PROCESS_ID.to_string()).await
}

#[get("/<pid>/<id>", format = "json")]
async fn get_doc_type(db: &State<KeyStore>, id: String, pid: String) -> ApiResponse {
    match db.get_document_type(&id).await{
        //TODO: would like to send "{}" instead of "null" when dt is not found
        Ok(dt) => ApiResponse::SuccessOk(json!(dt)),
        Err(e) => {
            error!("Error while retrieving doctype: {:?}", e);
            ApiResponse::InternalError(format!("Error while retrieving document type with id {} and pid {}!", id, pid))
        }
    }
}

#[get("/", format = "json")]
async fn get_doc_types(db: &State<KeyStore>) -> ApiResponse {
    match db.get_all_document_types().await {
        //TODO: would like to send "{}" instead of "null" when dt is not found
        Ok(dt) => ApiResponse::SuccessOk(json!(dt)),
        Err(e) => {
            error!("Error while retrieving default doctypes: {:?}", e);
            ApiResponse::InternalError(format!("Error while retrieving all document types"))
        }
    }
}

pub fn mount_api() -> AdHoc {
    AdHoc::on_ignite("Mounting Document Type API", |rocket| async {
        rocket
            .mount(ROCKET_DOC_TYPE_API, routes![create_doc_type,
                update_doc_type, delete_default_doc_type, delete_doc_type,
                get_default_doc_type, get_doc_type , get_doc_types])
    })
}