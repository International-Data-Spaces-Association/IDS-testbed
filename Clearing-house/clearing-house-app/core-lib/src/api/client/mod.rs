use rocket::fairing::{self, Fairing, Info, Kind};
use rocket::{Rocket, Build};
use crate::api::ApiClient;
use crate::api::client::daps_api::DapsApiClient;
use crate::api::client::keyring_api::KeyringApiClient;
use crate::api::client::document_api::DocumentApiClient;

pub mod document_api;
pub mod keyring_api;
pub mod daps_api;


#[derive(Clone, Debug)]
pub enum ApiClientEnum{
    Daps,
    Document,
    Keyring
}

#[derive(Clone, Debug)]
pub struct ApiClientConfigurator{
    api: ApiClientEnum,
}

impl ApiClientConfigurator{
    pub fn new(api: ApiClientEnum) -> Self{
        ApiClientConfigurator{
            api
        }
    }
}

#[rocket::async_trait]
impl Fairing for ApiClientConfigurator {
    fn info(&self) -> Info {
        match self.api {
            ApiClientEnum::Daps => {
                Info {
                    name: "Configuring Daps Api Client",
                    kind: Kind::Ignite
                }
            },
            ApiClientEnum::Document => {
                Info {
                    name: "Configuring Document Api Client",
                    kind: Kind::Ignite
                }
            },
            ApiClientEnum::Keyring => {
                Info {
                    name: "Configuring Keyring Api Client",
                    kind: Kind::Ignite
                }
            }
        }
    }

    async fn on_ignite(&self, rocket: Rocket<Build>) -> fairing::Result {
        let config_key = match self.api {
            ApiClientEnum::Daps => {
                debug!("Configuring Daps Api Client...");
                DapsApiClient::get_conf_param()
            },
            ApiClientEnum::Document => {
                debug!("Configuring Document Api Client...");
                DocumentApiClient::get_conf_param()
            },
            ApiClientEnum::Keyring => {
                debug!("Configuring Keyring Api Client...");
                KeyringApiClient::get_conf_param()
            }
        };
        let api_url: String = rocket.figment().extract_inner(&config_key).unwrap_or(String::new());
        if api_url.len() > 0 {
            debug!("...found api url: {}", &api_url);
            match self.api {
                ApiClientEnum::Daps => {
                    let client: DapsApiClient = ApiClient::new(&api_url);
                    Ok(rocket.manage(client))

                },
                ApiClientEnum::Document => {
                    let client: DocumentApiClient = ApiClient::new(&api_url);
                    Ok(rocket.manage(client))
                },
                ApiClientEnum::Keyring => {
                    let client: KeyringApiClient = ApiClient::new(&api_url);
                    Ok(rocket.manage(client))
                }
            }
        }
        else{
            error!("...api url not found in config file.");
            Err(rocket)
        }
    }
}