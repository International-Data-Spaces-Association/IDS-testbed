#[macro_use] extern crate error_chain;
#[macro_use] extern crate rocket;
#[macro_use] extern crate serde_derive;

use core_lib::util::setup_logger;
use rocket::{Build, Rocket};
use crate::db::KeyringDbConfigurator;
use core_lib::api::client::{ApiClientConfigurator, ApiClientEnum};
use core_lib::model::JwksCache;

mod api;
mod db;
mod crypto;
mod model;
#[cfg(test)] mod tests;

#[launch]
fn rocket() -> Rocket<Build> {
    // setup logging
    setup_logger().expect("Failure to set up the logger! Exiting...");

    rocket::build()
        .attach(api::key_api::mount_api())
        .attach(api::doc_type_api::mount_api())
        .attach(KeyringDbConfigurator)
        .attach(ApiClientConfigurator::new(ApiClientEnum::Daps))
        .manage(JwksCache::new())
}