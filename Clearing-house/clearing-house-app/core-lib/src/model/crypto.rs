use std::collections::HashMap;

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct KeyEntry {
    pub id: String,
    pub key: Vec<u8>,
    pub nonce: Vec<u8>,
}

impl KeyEntry{
    pub fn new(id: String, key: Vec<u8>, nonce: Vec<u8>)-> KeyEntry{
        KeyEntry{
            id,
            key,
            nonce
        }
    }
}

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct KeyMap {
    pub enc: bool,
    pub keys: HashMap<String, KeyEntry>,
    pub keys_enc: Option<Vec<u8>>,
}

impl KeyMap{
    pub fn new(enc: bool, keys: HashMap<String, KeyEntry>, keys_enc: Option<Vec<u8>>) -> KeyMap{
        KeyMap{
            enc,
            keys,
            keys_enc
        }
    }
 }

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct KeyCt{
    pub id: String,
    pub ct: String
}

impl KeyCt{
    pub fn new(id: String, ct: String) -> KeyCt{
        KeyCt{
            id,
            ct
        }
    }
}

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct KeyCtList {
    pub dt: String,
    pub cts: Vec<KeyCt>
}

impl KeyCtList{
    pub fn new(dt: String, cts: Vec<KeyCt>) -> KeyCtList{
        KeyCtList{
            dt,
            cts
        }
    }
}

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct KeyMapListItem {
    pub id: String,
    pub map: KeyMap
}

impl KeyMapListItem{
    pub fn new(id: String, map: KeyMap) -> KeyMapListItem{
        KeyMapListItem{
            id,
            map
        }
    }
}