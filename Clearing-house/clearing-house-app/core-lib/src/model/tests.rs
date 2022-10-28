use crate::model::crypto::{KeyEntry, KeyMap};
use crate::model::document::{Document, DocumentPart, EncryptedDocument};
use crate::errors::*;
use std::collections::HashMap;
use chrono::Utc;

fn create_test_doc(dt_id: String) -> Document{
    let mut doc_parts = vec!();
    doc_parts.push(DocumentPart::new(String::from("part1"), Some(String::from("MODEL_VERSION"))));
    doc_parts.push(DocumentPart::new(String::from("part2"), Some(String::from("CORRELATION_MESSAGE"))));
    Document::new(Document::create_uuid(), dt_id, 3241, doc_parts)
}

fn create_key_enc_map() -> KeyMap{
    let mut map = HashMap::new();
    let key1 = String::from("an example very very secret key.");
    let key2 = String::from("another totally very secret key.");
    let nonce1 = String::from("unique nonce");
    let nonce2 = String::from("second nonce");
    let key_ct = String::from("very secure key ct").into_bytes();

    let e1 = KeyEntry::new(String::from("1"), key1.into_bytes(), nonce1.into_bytes());
    let e2 = KeyEntry::new(String::from("2"), key2.into_bytes(), nonce2.into_bytes());
    map.insert(String::from("part1"), e1);
    map.insert(String::from("part2"), e2);

    return KeyMap::new(true, map, Some(key_ct));
}

fn create_key_dec_map() -> KeyMap{
    let mut map = HashMap::new();
    let key1 = String::from("an example very very secret key.");
    let key2 = String::from("another totally very secret key.");
    let nonce1 = String::from("unique nonce");
    let nonce2 = String::from("second nonce");

    let e1 = KeyEntry::new(String::from("1"), key1.into_bytes(), nonce1.into_bytes());
    let e2 = KeyEntry::new(String::from("2"), key2.into_bytes(), nonce2.into_bytes());
    map.insert(String::from("1"), e1);
    map.insert(String::from("2"), e2);

    return KeyMap::new(false, map, None);
}

#[test]
fn test_document_part_encryption() -> Result<()>{

    // prepare test data
    let part = DocumentPart::new(String::from("model_version"), Some(String::from("MODEL_VERSION")));
    let expected_ct = hex::decode("7F80228F5187DBD7FC6F7DA93510905102D39EF790FB84097EAC541E9DABF3D035FB4E910E6F52E3DB31C935").unwrap();

    // create key and nonce
    let key = String::from("an example very very secret key.");
    let nonce = String::from("unique nonce");

    // encrypt
    let ct = part.encrypt(key.as_bytes(), nonce.as_bytes())?;

    // check
    assert_eq!(expected_ct, ct, "Ciphertext mismatch");
    Ok(())
}

#[test]
fn test_document_part_decryption() -> Result<()>{

    // prepare test data
    let ct = hex::decode("7F80228F5187DBD7FC6F7DA93510905102D39EF790FB84097EAC541E9DABF3D035FB4E910E6F52E3DB31C935").unwrap();
    let expected_part = DocumentPart::new(String::from("model_version"), Some(String::from("MODEL_VERSION")));

    // create key and nonce
    let key = String::from("an example very very secret key.");
    let nonce = String::from("unique nonce");

    // decrypt
    let result = DocumentPart::decrypt(key.as_bytes(), nonce.as_bytes(), ct.as_slice())?;

    // check
    assert_eq!(expected_part.name, result.name, "Field name mismatch");
    assert_eq!(expected_part.content, result.content, "Content mismatch");

    Ok(())
}

#[test]
fn test_document_encryption() -> Result<()>{

    // prepare test data
    let dt = String::from("ids_message");
    let pid = String::from("test_pid");
    let doc = create_test_doc(dt.clone());
    let ts = Utc::now().timestamp();
    let key_ct = String::from("very secret key ciphertext");
    let mut cts = vec!();
    cts.push(String::from("1::4EBC3F1C2B8CB16C52E41424502FD112015D9C25919C2401514B5DD5B4233B65593CF0A4"));
    cts.push(String::from("2::FE2195305E95B9F931660CBA20B4707A1D92123022371CEDD2E70A538A8771EE7540D9F34845BBAEECEC"));
    let expected_doc = EncryptedDocument::new(doc.id.clone(), pid, dt, ts, 3241, key_ct, cts);

    // create KeyMap for encryption
    let keys = create_key_enc_map();

    // encrypt
    let result = doc.clone().encrypt(keys)?;

    // ids should match
    assert_eq!(result.id, expected_doc.id);

    //checking the cts
    for i in 0..result.cts.len()-1{
        //println!("cts: {}", &result.cts[i]);
        assert_eq!(expected_doc.cts[i], result.cts[i]);
        assert_eq!(expected_doc.cts[i], result.cts[i]);
    }

    Ok(())
}

#[test]
fn test_document_decryption() -> Result<()>{

    // prepare test data
    let mut cts = vec!();
    let ts = Utc::now().timestamp();
    cts.push(String::from("1::4EBC3F1C2B8CB16C52E41424502FD112015D9C25919C2401514B5DD5B4233B65593CF0A4"));
    cts.push(String::from("2::FE2195305E95B9F931660CBA20B4707A1D92123022371CEDD2E70A538A8771EE7540D9F34845BBAEECEC"));
    let dt = String::from("ids_message");
    let pid = String::from("test_pid");
    let key_ct = String::from("very secure key ct");
    let expected_doc = create_test_doc(dt.clone());
    let enc_doc = EncryptedDocument::new(expected_doc.id.clone(), pid, dt.clone(), ts, 3241, key_ct, cts);

    // create KeyMap for decryption
    let dec_keys = create_key_dec_map();

    // decrypt
    let result = enc_doc.decrypt(dec_keys.keys)?;

    // ids should match
    assert_eq!(result.id, expected_doc.id);

    //check document type
    assert_eq!(result.dt_id, expected_doc.dt_id);

    //checking the parts
    for i in 0..result.parts.len()-1{
        //println!("part: {} {}", result.parts[i].name, result.parts[i].content.as_ref().unwrap());
        assert_eq!(expected_doc.parts[i].name, result.parts[i].name);
        assert_eq!(expected_doc.parts[i].content, result.parts[i].content);
    }

    Ok(())
}

#[test]
fn test_encryption_hash() -> Result<()> {

    // prepare test data
    let mut cts = vec!();
    let ts_fixed = 1630413850;
    let expected_hash = String::from("eIiWaM874V6p3eeGnEEafDvcPJAzACKhXn0yEAVw0pnZNh+Lz7eLuMMtoIQ1mhY3huy0PN5h9ntZf3mBPcZkow==");
    cts.push(String::from("1::4EBC3F1C2B8CB16C52E41424502FD112015D9C25919C2401514B5DD5B4233B65593CF0A4"));
    cts.push(String::from("2::FE2195305E95B9F931660CBA20B4707A1D92123022371CEDD2E70A538A8771EE7540D9F34845BBAEECEC"));
    let dt = String::from("ids_message");
    let pid = String::from("test_pid");
    let tc = 3241;
    let key_ct = String::from("very secure key ct");
    let mut expected_doc = create_test_doc(dt.clone());
    // need to fix otherwise random id
    expected_doc.id = String::from("a9a30044-7dfd-476f-a217-db1dc27aeb75");

    let enc_doc = EncryptedDocument::new(expected_doc.id.clone(), pid, dt.clone(), ts_fixed, tc, key_ct, cts);
    let hash = enc_doc.hash();
    assert_eq!(expected_hash, hash);

    Ok(())
}