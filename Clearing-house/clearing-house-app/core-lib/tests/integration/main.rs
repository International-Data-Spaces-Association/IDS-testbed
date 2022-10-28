use reqwest::{Client, StatusCode};
use reqwest::header::{CONTENT_TYPE, HeaderValue};

use core_lib::constants::ROCKET_DOC_TYPE_API;
use core_lib::errors::*;
use core_lib::model::document::{Document, DocumentPart};

/// Update this token to run tests successfully that require authentication
pub const TOKEN: &'static str = "eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzY29wZXMiOlsiaWRzYzpJRFNfQ09OTkVDVE9SX0FUVFJJQlVURVNfQUxMIl0sImF1ZCI6Imlkc2M6SURTX0NPTk5FQ1RPUlNfQUxMIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJuYmYiOjE2MzUyNDEyNzgsImlhdCI6MTYzNTI0MTI3OCwianRpIjoiT0RBNE5EazRNemsxT0RZMU16TXlOamN4TlE9PSIsImV4cCI6MTYzNTI0NDg3OCwic2VjdXJpdHlQcm9maWxlIjoiaWRzYzpUUlVTVF9TRUNVUklUWV9QUk9GSUxFIiwicmVmZXJyaW5nQ29ubmVjdG9yIjoiaHR0cDovL2NvbnN1bWVyLWNvcmUuZGVtbyIsIkB0eXBlIjoiaWRzOkRhdFBheWxvYWQiLCJAY29udGV4dCI6Imh0dHBzOi8vdzNpZC5vcmcvaWRzYS9jb250ZXh0cy9jb250ZXh0Lmpzb25sZCIsInRyYW5zcG9ydENlcnRzU2hhMjU2IjoiYzE1ZTY1NTgwODhkYmZlZjIxNWE0M2QyNTA3YmJkMTI0ZjQ0ZmI4ZmFjZDU2MWMxNDU2MWEyYzFhNjY5ZDBlMCIsInN1YiI6IkE1OjBDOkE1OkYwOjg0OkQ5OjkwOkJCOkJDOkQ5OjU3OjNBOjA0OkM4OjdGOjkzOkVEOjk3OkEyOjUyOmtleWlkOkNCOjhDOkM3OkI2Ojg1Ojc5OkE4OjIzOkE2OkNCOjE1OkFCOjE3OjUwOjJGOkU2OjY1OjQzOjVEOkU4In0.iemDKZXE_RXFKkffqpweTAXBb6YX0spU0b5Ez1ncQzEyDNkJ5UtsZkwZz8WqfWOdPqMA74ShzLMwfEtao3DoO4DfWrvXFAYh8Y6hHJjHO44kPm4rUdcymUsVLXxcWd8Jszi6HjRHLaJ1-466s1akDQ7yQB0l8g9PP7BOlYr2I00HZ_b5wQOWtwT2PQxeWjkBzTgP8iycF7kIT6jgTHYDkOAwIdiMgNH_dPaxOPfxupz5vJQPuC1o9-IAyXtk-yC9GNI18YtjYpqizB-Nm5QGlUSSYMrB7tUKEc46471QaC4tR_LkYDrGnDtJHrH_fq0eEe6wIKoUcdt_VnI9Km-Hpw";
pub const TEST_CONFIG: &'static str = "config.yml";


mod document_api_client;
mod keyring_api_client;
mod daps_api_client;
mod token_validation;

fn create_test_document(pid: &String, dt_id: &String, tc: i64) -> Document{
    let p1 = DocumentPart::new(String::from("name"), Some(String::from("This is document part name.")));
    let p2 = DocumentPart::new(String::from("payload"), Some(String::from("This is document part payload.")));
    let p3 = DocumentPart::new(String::from("connector"), Some(String::from("This is document part connector.")));
    let pts = vec!(p1, p2, p3);
    let d = Document::new(pid.clone(), dt_id.clone(),tc, pts);
    d
}

fn create_dt_json(dt_id: &String, pid: &String) -> String{
    let begin_dt = r#"{"id":""#;
    let begin_pid = r#"","pid":""#;
    let rest = r#"","parts":[{"name":"name"},{"name":"payload"},{"name":"connector"}]}"#;

    let mut json = String::from(begin_dt);
    json.push_str(dt_id);
    json.push_str(begin_pid);
    json.push_str(pid);
    json.push_str(rest);
    return json
}

fn insert_test_doc_type_into_keyring(token: &String, pid: &String, dt_id: &String) -> Result<bool>{
    let client = Client::new();
    let dt_url = format!("http://localhost:8002{}", ROCKET_DOC_TYPE_API);

    let json_data = create_dt_json(dt_id, pid);

    println!("json_data: {}", json_data);

    println!("calling {}", &dt_url);
    let mut response = client
        .post(dt_url.as_str())
        .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
        .bearer_auth(token)
        .body(json_data).send()?;

    println!("Status Code: {}", &response.status());
    match response.status(){
        StatusCode::CREATED => {
            println!("Response: {}", response.text()?);
            Ok(true)
        },
        _ => {
            panic!("Couldn't prepare doc type for test");
        }
    }
}

fn delete_test_doc_type_from_keyring(token: &String, pid: &String, dt_id: &String) -> Result<bool>{
    let client = Client::new();
    let dt_url = format!("http://localhost:8002{}/{}/{}", ROCKET_DOC_TYPE_API, pid, dt_id);

    println!("calling {}", &dt_url);
    let mut response = client
        .delete(dt_url.as_str())
        .header(CONTENT_TYPE, HeaderValue::from_static("application/json"))
        .bearer_auth(token)
        .send()?;

    println!("Status Code: {}", &response.status());
    match response.status(){
        StatusCode::NO_CONTENT => {
            println!("Response: {}", response.text()?);
            Ok(true)
        },
        _ => {
            println!("Couldn't delete document type");
            Ok(false)
        }
    }
}