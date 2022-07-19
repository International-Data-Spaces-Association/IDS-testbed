// definition of daps constants
pub const DAPS_AUD: &'static str = "idsc:IDS_CONNECTORS_ALL";
pub const DAPS_JWKS: &'static str = ".well-known/jwks.json";
pub const DAPS_KID: &'static str = "default";
pub const DAPS_AUTHHEADER: &'static str = "Authorization";
pub const DAPS_AUTHBEARER: &'static str = "Bearer";
pub const DAPS_CERTIFICATES: &'static str = "certs";

// definition of config parameters (in config files)
pub const DATABASE_URL: &'static str = "database_url";
pub const DOCUMENT_API_URL: &'static str = "document_api_url";
pub const KEYRING_API_URL: &'static str = "keyring_api_url";
pub const DAPS_API_URL: &'static str = "daps_api_url";
pub const CLEAR_DB: &'static str = "clear_db";

// define here the config options from environment variables
pub const ENV_API_LOG_LEVEL: &'static str = "API_LOG_LEVEL";

// definition of rocket mount points
pub const ROCKET_DOC_API: &'static str = "/doc";
pub const ROCKET_DOC_TYPE_API: &'static str = "/doctype";
pub const ROCKET_POLICY_API: &'static str = "/policy";
pub const ROCKET_STATISTICS: &'static str = "/statistics";
pub const ROCKET_PROCESS_API: &'static str = "/process";
pub const ROCKET_KEYRING_API: &'static str = "/keyring";
pub const ROCKET_USER_API: &'static str = "/users";

// definition of database clients
pub const DOCUMENT_DB_CLIENT: &'static str = "document-api";
pub const KEYRING_DB_CLIENT: &'static str = "keyring-api";
pub const PROCESS_DB_CLIENT: &'static str = "clearing-house-api";

// definition of table names
pub const MONGO_DB: &'static str = "ch_ids";
pub const DOCUMENT_DB: &'static str = "document";
pub const KEYRING_DB: &'static str = "keyring";
pub const PROCESS_DB: &'static str = "process";
pub const MONGO_COLL_DOCUMENTS: &'static str = "documents";
pub const MONGO_COLL_DOC_TYPES: &'static str = "doc_types";
pub const MONGO_COLL_DOC_PARTS: &'static str = "parts";
pub const MONGO_COLL_PROCESSES: &'static str = "processes";
pub const MONGO_COLL_TRANSACTIONS: &'static str = "transactions";
pub const MONGO_COLL_MASTER_KEY: &'static str = "keys";

// definition of database fields
pub const MONGO_ID: &'static str = "id";
pub const MONGO_MKEY: &'static str = "msk";
pub const MONGO_PID: &'static str = "pid";
pub const MONGO_DT_ID: &'static str = "dt_id";
pub const MONGO_NAME: &'static str = "name";
pub const MONGO_OWNER: &'static str = "owner";
pub const MONGO_TS: &'static str = "ts";
pub const MONGO_TC: &'static str = "tc";

// definition of default database values
pub const DEFAULT_PROCESS_ID: &'static str = "default";
pub const MAX_NUM_RESPONSE_ENTRIES: u64 = 10000;
pub const DEFAULT_NUM_RESPONSE_ENTRIES: u64 = 100;

// split string symbols for vec_to_string and string_to_vec
pub const SPLIT_QUOTE: &'static str = "'";
pub const SPLIT_SIGN: &'static str = "~";
pub const SPLIT_CT: &'static str = "::";


// definition of file names and folders
pub const FOLDER_DB: &'static str = "db_init";
pub const FOLDER_DATA: &'static str = "data";
pub const FILE_DOC: &'static str = "document.json";
pub const FILE_DEFAULT_DOC_TYPE: &'static str = "init_db/default_doc_type.json";

// definition of special document parts
pub const PAYLOAD_PART: &'static str = "payload";