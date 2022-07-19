use reqwest::Client;
use reqwest::header::{HeaderValue, CONTENT_TYPE};
use serde_json;
use crate::api::ApiClient;
use crate::errors::*;
use crate::api::BlockchainMessage;

#[derive(Clone)]
pub struct BlockchainApiClient {
    uri: String,
}

impl ApiClient for BlockchainApiClient {
    fn new(uri: &str) -> BlockchainApiClient {
        let uri = String::from(uri);
        BlockchainApiClient {
            uri,
        }
    }
}

impl BlockchainApiClient {
    pub fn get_hash_list(&self, id: &String) -> Result<Vec<BlockchainMessage>>{
        let uri = format!("{}/hash/{}", self.uri, id);
        let client = Client::new();

        debug!("calling {}", &uri);
        let mut response = client.get(uri.as_str())
            .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
            .send()?;
        let hash_list: Vec<BlockchainMessage> = response.json()?;

        debug!("Status Code: {}", &response.status());
        Ok(hash_list)
    }

    pub fn store_hash(&self, id: &String, c_id: &String, hash: &String) -> Result<bool>{
        let uri = format!("{}/hash", self.uri);
        let client = Client::new();

        let m = BlockchainMessage::new(id.clone(), c_id.clone(), hash.clone());
        let payload = serde_json::to_string(&m)?;

        debug!("calling {}", &uri);
        let mut result = client.post(uri.as_str())
            .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
            .body(payload)
            .send()?;

        debug!("Status Code: {}", &result.status());
        debug!("result: {}", result.text()?);
        Ok(result.status().is_success())
    }
}