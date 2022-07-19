extern crate biscuit;
extern crate chrono;
extern crate fern;
extern crate mongodb;
#[macro_use] extern crate rocket;
#[macro_use] extern crate serde_derive;

#[macro_use] extern crate error_chain;
pub mod errors {
    // Create the Error, ErrorKind, ResultExt, and Result types
    error_chain!{
        foreign_links {
            Conversion(std::num::TryFromIntError);
            Figment(figment::Error);
            HexError(hex::FromHexError);
            Io(::std::io::Error) #[cfg(unix)];
            Mongodb(mongodb::error::Error);
            SetLogger(log::SetLoggerError);
            ParseLogLevel(log::ParseLevelError);
            Reqwest(reqwest::Error);
            SerdeJson(serde_json::error::Error);
            Uft8Error(std::string::FromUtf8Error);
            BiscuitError(biscuit::errors::Error);
        }
    }
}

pub mod api;
pub mod constants;
pub mod db;
pub mod model;
pub mod util;
