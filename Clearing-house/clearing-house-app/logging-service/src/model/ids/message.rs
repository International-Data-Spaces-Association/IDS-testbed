use std::collections::HashMap;
use core_lib::model::document::{Document, DocumentPart};
use crate::model::ids::{InfoModelDateTime, InfoModelId, SecurityToken, MessageType};
use crate::model::ids::InfoModelId::SimpleId;

pub const DOC_TYPE: &'static str = "IDS_MESSAGE";

const MESSAGE_ID: &'static str = "message_id";
const MODEL_VERSION: &'static str = "model_version";
const CORRELATION_MESSAGE: &'static str = "correlation_message";
const TRANSFER_CONTRACT: &'static str = "transfer_contract";
const ISSUED: &'static str = "issued";
const ISSUER_CONNECTOR: &'static str = "issuer_connector";
const CONTENT_VERSION: &'static str = "content_version";
/// const RECIPIENT_CONNECTOR: &'static str = "recipient_connector"; // all messages should contain the CH connector, so we skip this information
const SENDER_AGENT: &'static str = "sender_agent";
///const RECIPIENT_AGENT: &'static str = "recipient_agent";  // all messages should contain the CH agent, so we skip this information
const PAYLOAD: &'static str = "payload";
const PAYLOAD_TYPE: &'static str = "payload_type";

pub const RESULT_MESSAGE: &'static str = "ResultMessage";
pub const REJECTION_MESSAGE: &'static str = "RejectionMessage";
pub const MESSAGE_PROC_NOTIFICATION_MESSAGE: &'static str = "MessageProcessedNotificationMessage";

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct IdsMessage {
    //IDS name
    #[serde(rename = "@context")]
    // random id without context
    pub context: Option<HashMap<String, String>>,
    //IDS name
    #[serde(rename = "@type")]
    // random id without context
    pub type_message: MessageType,
    //IDS id name
    #[serde(rename = "@id", alias = "id", skip_serializing_if = "Option::is_none")]
    // random id without context
    pub id: Option<String>,
    //skip for IDS
    #[serde(skip)]
    // process id
    pub pid: Option<String>,
    //IDS name
    #[serde(rename = "ids:modelVersion", alias="modelVersion")]
    // Version of the Information Model against which the Message should be interpreted
    pub model_version: String,
    //IDS name
    #[serde(rename = "ids:correlationMessage", alias="correlationMessage", skip_serializing_if = "Option::is_none")]
    //  Correlated message, e.g. a response to a previous request
    pub correlation_message: Option<String>,
    //IDS name
    #[serde(rename = "ids:issued", alias="issued")]
    // Date of issuing the Message
    pub issued: InfoModelDateTime,
    //IDS name
    #[serde(rename = "ids:issuerConnector", alias="issuerConnector")]
    // The Connector which is the origin of the message
    pub issuer_connector: InfoModelId,
    //IDS name
    #[serde(rename = "ids:senderAgent", alias="senderAgent")]
    // The Agent which initiated the Message
    pub sender_agent: String,
    //IDS name
    #[serde(rename = "ids:recipientConnector", alias="recipientConnector", skip_serializing_if = "Option::is_none")]
    // The Connector which is the recipient of the message
    pub recipient_connector: Option<Vec<InfoModelId>>,
    //IDS name
    #[serde(rename = "ids:recipientAgent", alias="recipientAgent", skip_serializing_if = "Option::is_none")]
    // The Agent for which the Message is intended
    pub recipient_agent: Option<Vec<InfoModelId>>,
    //IDS name
    #[serde(rename = "ids:transferContract", alias="transferContract", skip_serializing_if = "Option::is_none")]
    // The contract which is (or will be) the legal basis of the data transfer
    pub transfer_contract: Option<String>,
    //IDS name
    #[serde(rename = "ids:contentVersion", alias="contentVersion", skip_serializing_if = "Option::is_none")]
    // The contract which is (or will be) the legal basis of the data transfer
    pub content_version: Option<String>,
    //IDS name
    #[serde(rename = "ids:securityToken", alias="securityToken", skip_serializing)]
    // Authorization
    pub security_token: Option<SecurityToken>,
    //IDS name
    #[serde(rename = "ids:authorizationToken", alias="authorizationToken", skip_serializing_if = "Option::is_none")]
    // Authorization
    pub authorization_token: Option<String>,
    //IDS name
    #[serde(skip_serializing_if = "Option::is_none")]
    // Authorization
    pub payload: Option<String>,
    //IDS name
    #[serde(skip_serializing_if = "Option::is_none")]
    // Authorization
    pub payload_type: Option<String>,
}


macro_rules! hashmap {
    ($( $key: expr => $val: expr ),*) => {{
         let mut map = ::std::collections::HashMap::new();
         $( map.insert($key, $val); )*
         map
    }}
}

impl Default for IdsMessage {
    fn default() -> Self {
        IdsMessage {
            context: Some(hashmap![
                "ids".to_string() => "https://w3id.org/idsa/core/".to_string(),
                "idsc".to_string() => "https://w3id.org/idsa/code/".to_string()
                ]
            ),
            type_message: MessageType::Message,
            id: Some(autogen("MessageProcessedNotification")),
            pid: None,
            model_version: "".to_string(),
            correlation_message: None,
            issued: InfoModelDateTime::new(),
            issuer_connector: InfoModelId::new("".to_string()),
            sender_agent: "https://w3id.org/idsa/core/ClearingHouse".to_string(),
            recipient_connector: None,
            recipient_agent: None,
            transfer_contract: None,
            content_version: None,
            security_token: None,
            authorization_token: None,
            payload: None,
            payload_type: None,
        }
    }
}

impl IdsMessage {
    pub fn processed(msg: IdsMessage) -> IdsMessage {
        let mut message = IdsMessage::clone(msg);
        message.id = Some(autogen(MESSAGE_PROC_NOTIFICATION_MESSAGE));
        message.type_message = MessageType::MessageProcessedNotification;
        return message;
    }

    pub fn return_result(msg: IdsMessage) -> IdsMessage {
        let mut message = IdsMessage::clone(msg);
        message.id = Some(autogen(RESULT_MESSAGE));
        message.type_message = MessageType::ResultMessage;
        return message;
    }

    pub fn error(msg: IdsMessage) -> IdsMessage {
        let mut message = IdsMessage::clone(msg);
        message.id = Some(autogen(REJECTION_MESSAGE));
        message.type_message = MessageType::RejectionMessage;
        return message;
    }

    fn clone(msg: IdsMessage) -> IdsMessage {
        IdsMessage {
            context: msg.context.clone(),
            type_message: msg.type_message.clone(),
            id: msg.id.clone(),
            pid: msg.pid.clone(),
            model_version: msg.model_version.clone(),
            correlation_message: msg.correlation_message.clone(),
            issued: msg.issued.clone(),
            issuer_connector: msg.issuer_connector.clone(),
            sender_agent: msg.sender_agent.clone(),
            recipient_connector: msg.recipient_connector.clone(),
            recipient_agent: msg.recipient_agent.clone(),
            transfer_contract: msg.transfer_contract.clone(),
            security_token: msg.security_token.clone(),
            authorization_token: msg.authorization_token.clone(),
            payload: msg.payload.clone(),
            content_version: msg.content_version.clone(),
            payload_type: msg.payload.clone()
        }
    }

    pub fn restore() -> IdsMessage{
        IdsMessage {
            type_message: MessageType::LogMessage,
            //TODO recipient_agent CH
            ..Default::default()
        }
    }
}

/// Conversion from Document to IdsMessage
///
/// note: Documents are converted into LogMessages. The LogMessage contains
/// the payload and payload type, which is the data that was stored previously.
/// All other fields of the LogMessage are meta data about the logging, e.g.
/// when the message was logged, etc.
///
/// meta data that we also need to store
/// - message_id
/// - pid
/// - model_version
/// - correlation_message
/// - issued
/// - issuer_connector
/// - sender_agent
/// - transfer_contract
/// - content_version
/// - security_token
/// - authorization_token
/// - payload
/// - payload_type
impl From<Document> for IdsMessage {

    fn from(doc: Document) -> Self {
        let mut m = IdsMessage::restore();
        // pid
        m.pid = Some(doc.pid.clone());
        // message_id
        let p_map = doc.get_parts_map();
        if let Some(v) = p_map.get(MESSAGE_ID) {
            m.id = Some(v.as_ref().unwrap().clone());
        }
        // model_version
        if let Some(v) = p_map.get(MODEL_VERSION) {
            m.model_version = v.as_ref().unwrap().clone();
        }

        // correlation_message
        if let Some(v) = p_map.get(CORRELATION_MESSAGE) {
            m.correlation_message = Some(v.as_ref().unwrap().clone());
        }

        // transfer_contract
        if let Some(v) = p_map.get(TRANSFER_CONTRACT) {
            m.transfer_contract = Some(v.as_ref().unwrap().clone());
        }

        // issued
        if let Some(v) = p_map.get(ISSUED) {
            match serde_json::from_str(v.as_ref().unwrap()) {
                Ok(date_time) => {
                    m.issued = date_time;
                },
                Err(e) => {
                    error!("Error while converting DateTimeStamp (field 'issued') from database: {}", e);
                }
            }
        }

        // issuer_connector
        if let Some(v) = p_map.get(ISSUER_CONNECTOR) {
            m.issuer_connector = InfoModelId::SimpleId(v.as_ref().unwrap().clone());
        }

        // content_version
        if let Some(v) = p_map.get(CONTENT_VERSION) {
            m.content_version = Some(v.as_ref().unwrap().clone());
        }

        // sender_agent
        if let Some(v) = p_map.get(SENDER_AGENT) {
            m.sender_agent = v.clone().unwrap();
        }

        // payload
        if let Some(v) = p_map.get(PAYLOAD) {
            m.payload = Some(v.as_ref().unwrap().clone());
        }

        // payload_type
        if let Some(v) = p_map.get(PAYLOAD_TYPE) {
            m.payload_type = Some(v.as_ref().unwrap().clone());
        }

        //TODO: security_token
        //TODO: authorization_token

        m
    }
}

/// Conversion from IdsMessage to Document
/// 
/// most important part to store:
/// payload and payload type
/// 
/// meta data that we also need to store
/// - message_id
/// - pid
/// - model_version
/// - correlation_message
/// - issued
/// - issuer_connector
/// - sender_agent
/// - transfer_contract
/// - content_version
/// - security_token
/// - authorization_token
/// - payload
/// - payload_type
impl From<IdsMessage> for Document {
    fn from(m: IdsMessage) -> Self {
        let mut doc_parts = vec![];

        // message_id
        let id = match m.id {
            Some(m_id) => m_id,
            None => autogen("Message"),
        };

        doc_parts.push(DocumentPart::new(
            MESSAGE_ID.to_string(),
            Some(id),
        ));

        // model_version
        doc_parts.push(DocumentPart::new(
            MODEL_VERSION.to_string(),
            Some(m.model_version),
        ));

        // correlation_message
        doc_parts.push(DocumentPart::new(
            CORRELATION_MESSAGE.to_string(),
            m.correlation_message,
        ));

        // issued
        doc_parts.push(DocumentPart::new(
            ISSUED.to_string(),
            serde_json::to_string(&m.issued).ok()
        ));

        // issuer_connector
        doc_parts.push(DocumentPart::new(
            ISSUER_CONNECTOR.to_string(),
            Some(m.issuer_connector.to_string()),
        ));

        // sender_agent
        doc_parts.push(DocumentPart::new(
            SENDER_AGENT.to_string(),
            Some(m.sender_agent.to_string())
        ));

        // transfer_contract
        doc_parts.push(DocumentPart::new(
            TRANSFER_CONTRACT.to_string(),
            m.transfer_contract,
        ));

        // content_version
        doc_parts.push(DocumentPart::new(
            CONTENT_VERSION.to_string(),
            m.content_version,
        ));

        // security_token
        //TODO

        // authorization_token
        //TODO

        // payload
        doc_parts.push(DocumentPart::new(
            PAYLOAD.to_string(),
            m.payload.clone()
        ));

        // payload_type
        doc_parts.push(DocumentPart::new(
            PAYLOAD_TYPE.to_string(),
            m.payload_type.clone()
        ));

        // pid
        Document::new(m.pid.unwrap(), DOC_TYPE.to_string(), -1, doc_parts)
    }
}

fn autogen(message: &str) -> String {
    ["https://w3id.org/idsa/autogen/", message, "/", &Document::create_uuid()].concat()
}