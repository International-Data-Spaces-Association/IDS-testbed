use crate::crypto::generate_random_seed;
use hkdf::Hkdf;
use sha2::Sha256;
use core_lib::model::new_uuid;

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct MasterKey {
    pub id: String,
    pub key: String,
    pub salt: String
}

impl MasterKey{
    pub fn new(id: String, key: String, salt: String)-> MasterKey{
        MasterKey{
            id,
            key,
            salt
        }
    }

    pub fn new_random() -> MasterKey{
        let key_salt = generate_random_seed();
        let ikm = generate_random_seed();
        let (master_key, _) = Hkdf::<Sha256>::extract(Some(&key_salt), &ikm);

        MasterKey{
            id: new_uuid(),
            key: hex::encode_upper(master_key),
            salt: hex::encode_upper(generate_random_seed())
        }
    }
}