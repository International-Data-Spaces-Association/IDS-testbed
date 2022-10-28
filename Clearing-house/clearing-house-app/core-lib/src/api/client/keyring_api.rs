use reqwest::Client;
use reqwest::header::{CONTENT_TYPE, HeaderValue};
use crate::api::ApiClient;
use crate::errors::*;
use crate::constants::{ROCKET_KEYRING_API, KEYRING_API_URL};
use crate::model::crypto::{KeyMap, KeyMapListItem, KeyCtList};

#[derive(Clone)]
pub struct KeyringApiClient {
    uri: String,
}

impl ApiClient for KeyringApiClient {
    fn new(uri: &str) -> KeyringApiClient {
        let uri = String::from(uri);
        KeyringApiClient { uri }
    }

    fn get_conf_param() -> String {
        String::from(KEYRING_API_URL)
    }
}

impl KeyringApiClient {

    /// Calls the keyring api to generate new aes keys
    pub fn generate_keys(&self, token: &String, pid: &str, dt_id: &str) -> Result<KeyMap> {
        let keys_url = format!("{}{}/generate_keys/{}", self.uri, ROCKET_KEYRING_API, pid);
        let client = Client::new();

        debug!("calling {}", &keys_url);
        let mut result = client.get(keys_url.as_str())
            .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
            .bearer_auth(token)
            .query(&[("dt_id", dt_id)])
            .send()?;

        debug!("Status Code: {}", result.status());
        let key_map: KeyMap = result.json()?;
        trace!("Payload: {:?}", key_map);
        Ok(key_map)
    }

    /// Calls the keyring api to decrypt aes keys
    pub fn decrypt_keys(&self, token: &String, pid: &str, dt_id: &str, ct: &[u8]) -> Result<KeyMap>{
        let keys_url = format!("{}{}/decrypt_keys/{}/{}", self.uri, ROCKET_KEYRING_API, pid, hex::encode_upper(ct));
        let client = Client::new();

        debug!("calling {}", &keys_url);
        let mut result = client.get(keys_url.as_str())
            .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
            .bearer_auth(token)
            .query(&[("dt_id", dt_id)])
            .send()?;

        debug!("Status Code: {}", &result.status());
        let key_map: KeyMap = result.json()?;
        trace!("Payload: {:?}", key_map);
        Ok(key_map)
    }

    /// Calls the keyring api to decrypt aes keys
    pub fn decrypt_multiple_keys(&self, token: &String, pid: &str, cts: &KeyCtList) -> Result<Vec<KeyMapListItem>>{
        let keys_url = format!("{}{}/decrypt_keys/{}", self.uri, ROCKET_KEYRING_API, pid);
        let client = Client::new();

        let json_data = serde_json::to_string(cts)?;

        debug!("calling {}", &keys_url);
        let mut result = client.get(keys_url.as_str())
            .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
            .bearer_auth(token)
            .body(json_data)
            .send()?;

        debug!("Status Code: {}", &result.status());
        let key_maps: Vec<KeyMapListItem> = result.json()?;
        trace!("Payload: {:?}", key_maps);
        Ok(key_maps)
    }
}