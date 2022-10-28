use core_lib::constants::{MONGO_ID, MONGO_PID, MONGO_COLL_DOC_TYPES};
use core_lib::errors::*;
use rocket::futures::TryStreamExt;
use mongodb::bson::doc;

use crate::db::KeyStore;
use crate::model::doc_type::DocumentType;

impl KeyStore {
    // DOCTYPE
    pub async fn add_document_type(&self, doc_type: DocumentType) -> Result<()> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        match coll.insert_one(doc_type.clone(), None).await {
            Ok(_r) => {
                debug!("added new document type: {}", &_r.inserted_id);
                Ok(())
            },
            Err(e) => {
                error!("failed to log document type {}", &doc_type.id);
                Err(Error::from(e))
            }
        }
    }

    //TODO: Do we need to check that no documents of this type exist before we remove it from the db?
    pub async fn delete_document_type(&self, id: &String, pid: &String) -> Result<bool> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        let result = coll.delete_many(doc! { MONGO_ID: id, MONGO_PID: pid }, None).await?;
        if result.deleted_count >= 1 {
            Ok(true)
        } else {
            Ok(false)
        }
    }


    /// checks if the model exits
    pub async fn exists_document_type(&self, pid: &String, dt_id: &String) -> Result<bool> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        let result = coll.find_one(Some(doc! { MONGO_ID: dt_id, MONGO_PID: pid }), None).await?;
        match result {
            Some(_r) => Ok(true),
            None => {
                debug!("document type with id {} and pid {:?} does not exist!", &dt_id, &pid);
                Ok(false)
            }
        }
    }

    pub async fn get_all_document_types(&self) -> Result<Vec<DocumentType>> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        let result = coll.find(None, None).await?
            .try_collect().await.unwrap_or_else(|_| vec![]);
        Ok(result)
    }

    pub async fn get_document_type(&self, dt_id: &String) -> Result<Option<DocumentType>> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        debug!("get_document_type for dt_id: '{}'", dt_id);
        match coll.find_one(Some(doc! { MONGO_ID: dt_id}), None).await{
            Ok(result) => Ok(result),
            Err(e) => {
                error!("error while getting document type with id {}!", dt_id);
                Err(Error::from(e))
            }
        }
    }

    pub async fn update_document_type(&self, doc_type: DocumentType, id: &String) -> Result<bool> {
        let coll = self.database.collection::<DocumentType>(MONGO_COLL_DOC_TYPES);
        match coll.replace_one(doc! { MONGO_ID: id}, doc_type, None).await{
            Ok(r) => {
                if r.matched_count != 1 || r.modified_count != 1{
                    warn!("while replacing doc type {} matched '{}' dts and modified '{}'", id, r.matched_count, r.modified_count);
                }
                else{
                    debug!("while replacing doc type {} matched '{}' dts and modified '{}'", id, r.matched_count, r.modified_count);
                }
                Ok(true)
            },
            Err(e) => {
                error!("error while updating document type with id {}: {:#?}", id, e);
                Ok(false)
            }
        }
    }
}