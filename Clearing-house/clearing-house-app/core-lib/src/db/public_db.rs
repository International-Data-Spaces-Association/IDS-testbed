use crate::mongodb::{
    Bson,
    db::ThreadedDatabase,
    doc,
    coll::options::FindOneAndUpdateOptions
};
use crate::constants::{MONGO_ID, MONGO_PID, MONGO_DT_ID, MONGO_COLL_DOCUMENTS};
use crate::db::DataStore;
use crate::errors::*;
use crate::model::document::EncryptedDocument;
use rocket_contrib::json;

impl DataStore {

    // DOCUMENT

    pub fn add_document(&self, doc: EncryptedDocument) -> Result<bool> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        println!("add_document({:?})", json!(doc));
        let serialized_bson = mongodb::to_bson(&doc)?;
        match serialized_bson.as_document(){
            Some(document) => {
                match coll.insert_one(document.clone(), None) {
                    Ok(res) => {
                        println!("inserted document: acknowledged:{:?} inserted_id:{:?}", res.acknowledged, res.inserted_id);
                        Ok(true)
                    },
                    Err(e) => {
                        bail!("error_ insertion of document failed: {}", e);
                    }
                }
            },
            _ => bail!("conversion to document failed!"),
        }
    }

    /// deletes model from db
    pub fn delete_document(&self, id: &String) -> Result<bool> {
        // The model collection
        debug!("trying to delete entry with id '{}'", id);
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        let result = coll.delete_one(doc! { MONGO_ID: id }, None)?;
        if result.deleted_count == 1{
            Ok(true)
        }
        else{
            debug!("deleted_count={}", result.deleted_count);
            Ok(false)
        }
    }

    /// checks if the document exists
    /// document ids are globally unique
    pub fn exists_document(&self, id: &String) -> Result<bool> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        let result = coll.find_one(Some(doc! { MONGO_ID: id.clone() }), None)?;
        match result {
            Some(_r) => Ok(true),
            None => {
                debug!("document with id '{}' does not exist!", &id);
                Ok(false)
            }
        }
    }

    /// gets the model from the db
    pub fn get_document(&self, id: &String, pid: &String) -> Result<Option<EncryptedDocument>> {
        debug!("Looking for doc: {}", &id);
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        let result = coll.find_one(Some(doc! { MONGO_ID: id.clone(), MONGO_PID: pid.clone() }), None)?;

        match result {
            Some(r) => {
                let doc = mongodb::from_bson::<EncryptedDocument>(Bson::Document(r))?;
                Ok(Some(doc))
            },
            None => {
                Ok(None)
            }
        }
    }

    /// gets documents for a single process from the db
    pub fn get_documents_for_pid(&self, pid: &String) -> Result<Vec<EncryptedDocument>> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        // Create cursor that finds all documents
        let mut cursor = coll.find(Some(doc! { MONGO_PID: pid.clone() }), None)?;
        let mut result = vec!();

        loop{
            if cursor.has_next()?{
                // we checked has_next() so unwrap() is safe to get to the Result
                let d = cursor.next().unwrap()?;
                let doc = mongodb::from_bson::<EncryptedDocument>(Bson::Document(d))?;
                result.push(doc);
            }
            else{
                break;
            }
        }
        Ok(result)
    }

    /// gets documents of a specific document type for a single process from the db
    pub fn get_documents_of_dt_for_pid(&self, dt_id: &String, pid: &String) -> Result<Vec<EncryptedDocument>> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        // Create cursor that finds all documents
        let mut cursor = coll.find(Some(doc! { MONGO_PID: pid.clone(), MONGO_DT_ID: dt_id.clone() }), None)?;
        let mut result = vec!();

        loop{
            if cursor.has_next()?{
                // we checked has_next() so unwrap() is safe to get to the Result
                let d = cursor.next().unwrap()?;
                let doc = mongodb::from_bson::<EncryptedDocument>(Bson::Document(d))?;
                result.push(doc);
            }
            else{
                break;
            }
        }
        Ok(result)
    }

    /// gets all documents from the db
    pub fn get_all_documents(&self) -> Result<Vec<EncryptedDocument>> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        // Create cursor that finds all documents
        let mut cursor = coll.find(None, None)?;
        let mut result = vec!();

        loop{
            if cursor.has_next()?{
                // we checked has_next() so unwrap() is safe to get to the Result
                let d = cursor.next().unwrap()?;
                let doc = mongodb::from_bson::<EncryptedDocument>(Bson::Document(d))?;
                result.push(doc);
            }
            else{
                break;
            }
        }
        Ok(result)
    }

    /// update existing model in the db
    pub fn update_document(&self, doc: EncryptedDocument) -> Result<bool> {
        // The model collection
        let coll = self.database.collection(MONGO_COLL_DOCUMENTS);
        let serialized_doc = mongodb::to_bson(&doc).unwrap(); // Serialize

        let mut options = FindOneAndUpdateOptions::new();
        options.upsert = Some(true);

        let result = coll.find_one_and_replace(doc! { MONGO_ID: doc.id.clone() },
                                               serialized_doc.as_document().unwrap().clone(),
                                               Some(options))?;
        match result {
            Some(r) => {
                let old_doc = mongodb::from_bson::<EncryptedDocument>(Bson::Document(r))?;
                debug!("old model type was: {}", &old_doc.id);
                Ok(true)
            },
            None => {
                warn!("model type with id {} could not be updated!", &doc.id);
                Ok(false)
            }
        }
    }
}