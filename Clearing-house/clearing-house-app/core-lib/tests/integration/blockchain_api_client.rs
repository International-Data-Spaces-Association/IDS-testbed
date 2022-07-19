use core_lib::constants::{CONFIG_FILE, BLOCKCHAIN_API_URL};
use core_lib::util;
use core_lib::errors::*;
use core_lib::api::client::blockchain_api::BlockchainApiClient;

/// before running make sure the blockchain api is available
#[test]
fn test_store_hash() -> Result<()>{
    // configure client_api
    let config = util::load_config(CONFIG_FILE);
    let bc_api: BlockchainApiClient = util::configure_api(BLOCKCHAIN_API_URL, &config)?;

    let id = String::from("999");
    let cid = String::from("123");
    let hash = String::from("ABCD-EFGH");

    assert_eq!(bc_api.store_hash(&id, &cid, &hash)?, true);

    Ok(())
}

#[test]
fn test_get_hash_list() -> Result<()>{
    // configure client_api
    let config = util::load_config(CONFIG_FILE);
    let bc_api: BlockchainApiClient = util::configure_api(BLOCKCHAIN_API_URL, &config)?;

    let id = String::from("999");
    let cid1 = String::from("123");
    let hash1 = String::from("ABCD-EFGH");
    let cid2 = String::from("5556");
    let hash2 = String::from("ZAZS-QWEA");

    assert_eq!(bc_api.store_hash(&id, &cid1, &hash1)?, true);
    assert_eq!(bc_api.store_hash(&id, &cid2, &hash2)?, true);

    let result = bc_api.get_hash_list(&id)?;

    assert_eq!(result.len(), 2);

    Ok(())
}