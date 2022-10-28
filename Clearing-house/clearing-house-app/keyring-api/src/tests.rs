use core_lib::errors::*;
use crate::model::doc_type::{DocumentType, DocumentTypePart};
use crate::crypto::{encrypt_secret, decrypt_secret, generate_key_map, restore_key_map};
use crate::model::crypto::MasterKey;

fn create_test_document_type() -> DocumentType{
    let mut parts = vec!();
    parts.push(DocumentTypePart::new(String::from("name")));
    parts.push(DocumentTypePart::new(String::from("message")));
    parts.push(DocumentTypePart::new(String::from("connector")));

    DocumentType::new(String::from("test_dt_1"), String::from("test_pid_1"), parts)
}

#[test]
fn test_key_generation() -> Result<()>{
    // prepare test data
    let dt = create_test_document_type();
    let k = String::from("C36D50B35B5981C8F1FAD6738848BD5A4F77EF77B56A4E66F7961B9B7A642B2B");
    let salt = String::from("A6E804FF70117E606686EDD8516C95734E239453AB52AC6E3F916D1D861412B574A91B01ECE5F9E4A17B498EDA132792CC9A89C031470950F87AE402B8DDA581410D7E310A5E4204F1467A4E4C240CCB180A84A1B1DE2A06FDB4474C98E78026FDCFB862DE7AC60A4A6772268EE397AF18C28F41DD9A10471E469833EB2092E28AE8D3DD58D98ACC521FC87B99A19912F70376F7E3026C960F903FE7B44F1903A5E36313EE1A8A60B2E317A6443B9408ABBA2763BD3ED42F406F5F19551ED84ADDAD0CD8A652ED72F0040E44CCF3C6CF854D5EA6FBFE9267DB4EBFAD5DE9BA3055049D71CC64A90B081C2A37ED0B5FDDB88AE864436A7D1F14FCA1F969B67F9E");
    let id = String::from("86177e93-29aa-477a-b63f-03ccd9c5679d");
    let mkey = MasterKey::new(id, k, salt);

    // run the test
    let keys = generate_key_map(mkey, dt)?;

    // Keymap generated for encryption
    assert_eq!(keys.enc, true);

    // there should be 3 items in the hash map
    assert_eq!(keys.keys.len(), 3);

    // no key should be the same as another
    keys.keys.values().for_each(|i| {
        keys.keys.values().for_each(|j|{
            if i.id.ne(&j.id){
                assert!(i.nonce.ne(&j.nonce));
                assert!(i.key.ne(&j.key));
            }
        });
    });

    Ok(())
}

#[test]
fn test_restoring_keymap() -> Result<()>{
    // prepare test data
    let dt = create_test_document_type();
    let keys_ct = hex::decode("29D816635437C4487DACD93349F6B853EAD8C6F37250901A5BEEF1529E2358BBE634E6D1BD923ED0F2F842DB83139A9786796190DA8DF8F09F0384C8842BA0316079F857C71184C0C4E2A74622D0BED7").unwrap();
    let k = String::from("C36D50B35B5981C8F1FAD6738848BD5A4F77EF77B56A4E66F7961B9B7A642B2B");
    let salt = String::from("A6E804FF70117E606686EDD8516C95734E239453AB52AC6E3F916D1D861412B574A91B01ECE5F9E4A17B498EDA132792CC9A89C031470950F87AE402B8DDA581410D7E310A5E4204F1467A4E4C240CCB180A84A1B1DE2A06FDB4474C98E78026FDCFB862DE7AC60A4A6772268EE397AF18C28F41DD9A10471E469833EB2092E28AE8D3DD58D98ACC521FC87B99A19912F70376F7E3026C960F903FE7B44F1903A5E36313EE1A8A60B2E317A6443B9408ABBA2763BD3ED42F406F5F19551ED84ADDAD0CD8A652ED72F0040E44CCF3C6CF854D5EA6FBFE9267DB4EBFAD5DE9BA3055049D71CC64A90B081C2A37ED0B5FDDB88AE864436A7D1F14FCA1F969B67F9E");
    let id = String::from("86177e93-29aa-477a-b63f-03ccd9c5679d");
    let mkey = MasterKey::new(id, k, salt);

    let mut expected_keys = vec!();
    expected_keys.push(hex::decode("0FCBA316FA47AC0E3EFF4D69B7780925ED22CFF46FC1A731B4E9942FED67BA04").unwrap());
    expected_keys.push(hex::decode("DE888EF80B13390CA76387F18528F3B3948B8C446D70C09F7C2A1D2346CFE917").unwrap());
    expected_keys.push(hex::decode("2E6953A92D081C5189DED6FB9644606257A2839CD2159F77166DF246E236B67C").unwrap());

    let mut expected_nonces = vec!();
    expected_nonces.push(hex::decode("6A63BE704DC9687FA3FDFF26").unwrap());
    expected_nonces.push(hex::decode("D0E2744835BD2FFECFFA9AE6").unwrap());
    expected_nonces.push(hex::decode("83587A962A24F94D907CF2B7").unwrap());

    // run the test
    let result = restore_key_map(mkey, dt, keys_ct)?;

    // Keymap generated for decryption
    assert_eq!(result.enc, false);

    // there should be 3 items in the hash map
    assert_eq!(result.keys.len(), 3);

    // check the derived keys and nonces
    result.keys.values().for_each(|i| {
        let index = i.id.parse::<usize>().unwrap();
        assert_eq!(i.key, expected_keys[index]);
        assert_eq!(i.nonce, expected_nonces[index]);
    });

    Ok(())
}


#[test]
fn test_encrypting_secret() -> Result<()>{
    // prepare test data
    let key = hex::decode("9530D8826CCE9D6CF377B849D63C7155F78343120A303D55F1A9BECAF25E9713").unwrap();
    let nonce = hex::decode("2C0802076377687B9A403120").unwrap();
    let secret = String::from("1EB18B9FC8CBA07F2EA00BC00FBE468AB1D48E2E28F14FAD61EA3A38B41E2586");
    let expected_ct = hex::decode("CAE855AF0FD950A25F2D629A344F2B51530EE98990A77D4B49868C3EB497913A9E936D9DBF9487A77A7B36709C8F1AE43A40D779D7D56351A606675A04FCE5F8B7E80C06B3E9A47083C2E604AD5F681D").unwrap();

    // run the test
    let result = encrypt_secret(key.as_slice(), nonce.as_slice(), secret.clone())?;

    assert_eq!(expected_ct, result);

    Ok(())
}

#[test]
fn test_decrypting_secret() -> Result<()>{
    // prepare test data
    let key = hex::decode("9530D8826CCE9D6CF377B849D63C7155F78343120A303D55F1A9BECAF25E9713").unwrap();
    let nonce = hex::decode("2C0802076377687B9A403120").unwrap();
    let ct = hex::decode("CAE855AF0FD950A25F2D629A344F2B51530EE98990A77D4B49868C3EB497913A9E936D9DBF9487A77A7B36709C8F1AE43A40D779D7D56351A606675A04FCE5F8B7E80C06B3E9A47083C2E604AD5F681D").unwrap();
    let expected_secret = String::from("1EB18B9FC8CBA07F2EA00BC00FBE468AB1D48E2E28F14FAD61EA3A38B41E2586");

    // run the test
    let result = decrypt_secret(key.as_slice(), nonce.as_slice(), ct.as_slice())?;

    // check the decryption
    assert_eq!(expected_secret, result);

    Ok(())
}
