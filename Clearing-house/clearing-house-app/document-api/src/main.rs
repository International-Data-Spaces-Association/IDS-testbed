#[macro_use] extern crate rocket;

use core_lib::api::client::{ApiClientConfigurator, ApiClientEnum};
use core_lib::util::setup_logger;
use rocket::fairing::AdHoc;
use rocket::http::Method;
use rocket::{Rocket, Build};
use rocket_cors::{
    AllowedHeaders, AllowedOrigins,
    CorsOptions
};
use core_lib::model::JwksCache;
use crate::db::DatastoreConfigurator;

mod doc_api;
mod db;

fn add_cors_options() ->  AdHoc {
    AdHoc::on_ignite("Adding CORS rules", |rocket| async {
        let allowed_origins = AllowedOrigins::some_exact(&[
            "http://127.0.0.1",
            "http://127.0.0.1:4200",
            "http://127.0.0.1:8001",
            "http://localhost",
            "http://localhost:4200",
            "http://localhost:8001",
            "http://document-gui",
            "http://document-gui.local",
            "https://127.0.0.1",
            "https://127.0.0.1:4200",
            "https://127.0.0.1:8001",
            "https://localhost",
            "https://localhost:4200",
            "https://localhost:8001",
            "https://document-gui",
            "https://document-gui.local"
        ]);

        let cors_options = CorsOptions {
            allowed_origins,
            allowed_methods: vec![Method::Get, Method::Post, Method::Options, Method::Delete].into_iter().map(From::from).collect(),
            allowed_headers: AllowedHeaders::some(&[
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Methods",
                "Access-Control-Allow-Headers",
                "Accept",
                "Authorization",
                "Content-Type",
                "Origin"
            ]),
            allow_credentials: true,
            ..Default::default()
        }.to_cors();

        match cors_options {
            Ok(cors) => rocket.attach(cors),
            Err(_) => rocket
        }
    })
}

#[launch]
fn rocket() -> Rocket<Build> {
    // setup logging
    setup_logger().expect("Failure to set up the logger! Exiting...");

    rocket::build()
        .attach(doc_api::mount_api())
        .attach(add_cors_options())
        .attach(DatastoreConfigurator)
        .attach(ApiClientConfigurator::new(ApiClientEnum::Daps))
        .attach(ApiClientConfigurator::new(ApiClientEnum::Keyring))
        .manage(JwksCache::new())
}