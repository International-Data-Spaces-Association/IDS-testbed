use biscuit::{
    CompactJson,
    jwa::SignatureAlgorithm,
    jwk::JWKSet,
    JWT,
    CompactPart,
    Empty,
    ClaimsSet,
    ValidationOptions,
};
use rocket::outcome::IntoOutcome;
use rocket::request::{self, Request, FromRequest};
use serde::{Deserialize, Serialize};
use serde::de::DeserializeOwned;
use std::fmt::Debug;
use biscuit::jws::Compact;

use crate::{
    constants::{
        DAPS_AUTHHEADER,
        DAPS_AUTHBEARER,
    },
    errors::*,
    api::client::daps_api::DapsApiClient,
};
use crate::api::claims::IdsClaims;
use crate::model::JwksCache;

#[derive(Debug, PartialEq, Clone, Serialize, Deserialize)]
pub struct ApiKey<T, H> {
    pub token: JWT<T, H>,
    pub raw: String,
}

impl<T, H> ApiKey<T, H>
    where
        T: CompactPart + Debug + Clone,
        H: Serialize + DeserializeOwned + Clone,
        ClaimsSet<T>: CompactPart {
    pub fn new(token: JWT<T, H>, raw: String) -> ApiKey<T, H> {
        ApiKey::<T, H> {
            token,
            raw,
        }
    }
    pub fn raw(&self) -> String {
        self.raw.clone()
    }
    pub fn issuer(&self) -> Option<String> {
        self
            .token
            .clone()
            .payload()
            .unwrap()
            .registered
            .issuer
            .clone()
    }
    pub fn sub(&self) -> Option<String> {
        self
            .token
            .clone()
            .payload()
            .unwrap()
            .registered
            .subject
            .clone()
    }
    pub fn claims(&self) -> ClaimsSet<T> {
        self
            .token
            .clone()
            .unwrap_decoded()
            .1
    }
}

#[rocket::async_trait]
impl<'r, T: DeserializeOwned + CompactJson + Debug + Clone, H: Serialize + DeserializeOwned + Clone> FromRequest<'r> for ApiKey<T, H> {
    type Error = ();

    async fn from_request(request: &'r Request<'_>) -> request::Outcome<Self, ()> {
        request.rocket().state::<DapsApiClient>()
            .map(|daps_client| {
                // check cache for jwks and if empty get from daps
                let mut cached = false;
                let mut cache_needs_update = false;
                let mut jwks = None;
                match request.rocket().state::<JwksCache>() {
                    Some(cache_lock) => {
                        jwks = match cache_lock.jwks.read() {
                            Ok(cache) => {
                                if cache.is_some() {
                                    debug!("...found cached jwks");
                                    cached = true;
                                    Some(cache.as_ref().unwrap().clone())
                                } else {
                                    debug!("...no jwks cached");
                                    match daps_client.get_jwks() {
                                        Ok(new_jwks) => {
                                            debug!("...got jwks from daps");
                                            cache_needs_update = true;
                                            Some(new_jwks)
                                        },
                                        Err(_e) => {
                                            error!("... failed to get jwks from daps!");
                                            None
                                        }
                                    }
                                }
                            },
                            Err(e) => {
                                error!("JwksCache is poisoned: {:#?}", e);
                                None
                            }
                        };
                    }
                    None => {
                        warn!("Caching is not working!");
                    }
                };

                // validate token using jwks
                let auth_header_value: Vec<_> = request
                    .headers()
                    .get(&DAPS_AUTHHEADER)
                    .collect();
                trace!("...received auth header: {:?}", &auth_header_value);
                if auth_header_value.len() != 1 {
                    return None;
                }
                let mut iter = auth_header_value[0].split_ascii_whitespace();
                match iter.next() {
                    Some(DAPS_AUTHBEARER) => {
                        let token = iter.next().unwrap_or("");
                        trace!("...found bearer token {:?}", &token);
                        match jwks{
                            Some(ref key_set) => {
                                let mut api_token = match validate_token(token, key_set, Some(daps_client.algorithm)) {
                                    Ok(validated_token) => {
                                        debug!("...valid token!");
                                        Some(validated_token)
                                    }
                                    Err(_e) => {
                                        warn!("...invalid token {:?}, {:?}", &token, _e);
                                        None
                                    }
                                };
                                // retry only if we used cached token and validation failed
                                if cached && api_token.is_none() {
                                    debug!("...maybe cache is dirty. Get jwks from daps...");
                                    match daps_client.get_jwks() {
                                        Ok(new_jwks) => {
                                            debug!("...retrieved...");
                                            match validate_token(token, &new_jwks, Some(daps_client.algorithm)) {
                                                Ok(validated_token) => {
                                                    debug!("...valid token!");
                                                    api_token = Some(validated_token);
                                                    cache_needs_update = true;
                                                    jwks = Some(new_jwks.clone());
                                                },
                                                Err(_e) => {
                                                    debug!("...still invalid token {:?}, {:?}", &token, _e);
                                                }
                                            };
                                        },
                                        Err(_e) =>{
                                            debug!("...failed to retrieve jwks from daps. Giving up.");
                                        }
                                    }
                                }

                                // update cache only if cache was empty or validation failed with cached jwks and succeeded with new jwks
                                if cache_needs_update{
                                    debug!("...updating jwks cache...");
                                    *request.rocket().state::<JwksCache>().unwrap().jwks.write().unwrap() = jwks.clone();
                                }
                                api_token
                            },
                            None => None
                        }
                    }
                    _ => None
                }

            })
            .expect("DAPS client not initialized")
            .or_forward(())
    }
}


pub fn validate_token<T: Serialize + for<'de> Deserialize<'de> + CompactJson + Debug + Clone, H: Serialize + for<'de> Deserialize<'de> + Clone>(token: &str, jwks: &JWKSet<Empty>, expected_algorithm: Option<SignatureAlgorithm>) -> Result<ApiKey<T, H>> {
    match JWT::new_encoded(token)
        .decode_with_jwks::<Empty>(jwks, expected_algorithm) {
        Ok(decoded_token) => {
            match decoded_token.validate(ValidationOptions::default()) {
                Ok(()) => Ok(ApiKey::new(decoded_token, token.to_string())),
                Err(e) => Err(Error::from(e))
            }
        }
        Err(e) => Err(Error::from(e))
    }
}
