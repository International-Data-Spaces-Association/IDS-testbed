use std::fs::File;
use std::io::Read;
use std::path::Path;
use biscuit::{jwa::SignatureAlgorithm, jwk::JWKSet};
use reqwest::{Certificate, Client, ClientBuilder};
use serde::de::DeserializeOwned;
use serde_json;
use crate::api::ApiClient;
use crate::errors::*;
use crate::constants::{DAPS_JWKS, DAPS_API_URL, DAPS_CERTIFICATES};

#[derive(Clone)]
pub struct DapsApiClient {
    uri: String,
    pub algorithm: SignatureAlgorithm,
    pub client: Client,
}

impl Default for DapsApiClient {
    fn default() -> Self {
        let cert_path = Path::new(DAPS_CERTIFICATES);
        let mut client_builder = ClientBuilder::new();
        match cert_path.read_dir(){
            Ok(fs) => {
                let certs: Vec<Certificate> = fs.filter_map(|entry|
                    if entry.is_ok(){
                        if let Ok(mut cert_file) = File::open(entry.as_ref().unwrap().path()){
                            let mut buf = Vec::new();
                            match cert_file.read_to_end(& mut buf){
                                Ok(_) => {
                                    debug!("Parsing certificate {} ...", entry.as_ref().unwrap().path().to_str().unwrap_or(""));
                                    if let Ok(cert) = reqwest::Certificate::from_der(&buf){
                                        debug!("... adding certificate as root certificate");
                                        return Some(cert)
                                    }
                                    else{
                                        warn!("... couldn't parse certificate. Not added.");
                                        return None
                                    }
                                }
                                Err(e) => {
                                    error!("Error while reading certificate file {}: {}", entry.as_ref().unwrap().file_name().to_str().unwrap_or(""), e);
                                    return None
                                }
                            };
                        }
                        else{
                            return None
                        }
                    }
                    else{
                        return None
                    })
                    .collect();
                for c in certs{
                    client_builder = client_builder.add_root_certificate(c);
                }
            }
            Err(e) => {
                error!("Error while trying to read certificates for DAPS: {:#?}", e);
            }
        }

        DapsApiClient {
            uri: "".to_string(),
            algorithm: SignatureAlgorithm::RS256,
            client: client_builder.build().unwrap()
        }
    }
}

impl ApiClient for DapsApiClient {
    fn new(s: &str) -> DapsApiClient {
        DapsApiClient {
            uri: String::from(s),
            ..Default::default()
        }
    }

    fn get_conf_param() -> String {
        String::from(DAPS_API_URL)
    }
}

impl DapsApiClient {
    pub fn get_jwks<J: DeserializeOwned>(&self) -> Result<JWKSet<J>>{
        let pk_url = format!("{}/{}", self.uri, DAPS_JWKS);
        debug!("trying to get JWKSet from url: {}", pk_url);
        match self.client.get(pk_url.as_str()).send() {
            Ok(mut resp) => {
                match serde_json::from_str(&resp.text().unwrap()) {
                    Ok(body) => {
                        Ok(body)
                    },
                    Err(e) =>{
                        error!("error while parsing answer from server: {:?}", e);
                        Err(Error::from(e))
                    }
                }
            },
            Err(e) => {
                error!("did not receive response from {}: {:?}", pk_url, e);
                Err(Error::from(e))
            },
        }
    }
}