use mongodb::Client;
use mongodb::options::ClientOptions;
use crate::errors::*;

pub trait DataStoreApi{
    fn new(client: Client) -> Self;
}

pub async fn init_database_client<T: DataStoreApi>(db_url: &str, client_name: Option<String>) -> Result<T>{
    let mut client_options;

    match ClientOptions::parse(&format!("{}", db_url)).await{
        Ok(co) => {client_options = co;}
        Err(_) => {
            bail!("Can't parse database connection string");
        }
    };

    client_options.app_name = client_name;
    let client = Client::with_options(client_options)?;
    Ok(T::new(client))
}