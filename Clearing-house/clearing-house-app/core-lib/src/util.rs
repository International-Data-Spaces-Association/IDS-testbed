use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};
use std::env;
use std::fs::File;
use std::io::prelude::*;
use std::str::FromStr;

use crate::constants::ENV_API_LOG_LEVEL;
use crate::errors::*;
use figment::{Figment, providers::{Format, Yaml}};

pub fn load_from_test_config(key: &str, file: &str) -> String{
    Figment::new().merge(Yaml::file(file)).extract_inner(key).unwrap_or(String::new())
}

/// setup the fern logger and set log level to environment variable `ENV_API_LOG_LEVEL`
/// allowed levels: `Off`, `Error`, `Warn`, `Info`, `Debug`, `Trace`
pub fn setup_logger() -> Result<()> {
    let log_level;
    match env::var(ENV_API_LOG_LEVEL){
        Ok(l) => log_level = l.clone(),
        Err(_e) => {
            println!("Log level not set correctly. Logging disabled");
            log_level = String::from("Off")
        }
    };

    fern::Dispatch::new()
        .format(|out, message, record| {
            out.finish(format_args!(
                "{}[{}][{}] {}",
                chrono::Local::now().format("[%Y-%m-%d][%H:%M:%S]"),
                record.target(),
                record.level(),
                message
            ))
        })
        .level(log::LevelFilter::from_str(&log_level.as_str())?)
        .chain(std::io::stdout())
        .chain(fern::log_file("output.log")?)
        .apply()?;
    Ok(())
}

pub fn read_file(file: &str) -> Result<String> {
    let mut f = File::open(file)?;
    let mut data = String::new();
    f.read_to_string(&mut data)?;
    drop(f);
    Ok(data)
}

pub fn url_encode(id: &str) -> String{
    utf8_percent_encode(id, NON_ALPHANUMERIC).to_string()
}