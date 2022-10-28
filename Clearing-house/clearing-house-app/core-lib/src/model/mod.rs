use std::sync::RwLock;
use biscuit::Empty;
use biscuit::jwk::JWKSet;

pub mod crypto;
pub mod document;
pub mod process;

#[cfg(test)] mod tests;

pub fn new_uuid() -> String {
    use uuid::Uuid;
    Uuid::new_v4().to_hyphenated().to_string()
}

#[derive(Debug, Clone, Serialize, Deserialize, FromFormField)]
pub enum SortingOrder{
    #[field(value = "asc")]
    #[serde(rename = "asc")]
    Ascending,
    #[field(value = "desc")]
    #[serde(rename = "desc")]
    Descending
}

#[derive(Debug)]
pub struct JwksCache{
    pub jwks: RwLock<Option<JWKSet<Empty>>>
}

impl JwksCache{
    pub fn new() -> JwksCache{
        JwksCache{
            jwks: RwLock::new(None)
        }
    }
}