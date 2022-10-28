use crate::model::ids::message::IdsMessage;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ClearingHouseMessage {
    pub header: IdsMessage,
    pub payload: Option<String>,
    #[serde(rename = "payloadType")]
    pub payload_type: Option<String>,
}

impl ClearingHouseMessage {
    pub fn new(header: IdsMessage, payload: Option<String>, payload_type: Option<String>) -> ClearingHouseMessage{
        ClearingHouseMessage{
            header,
            payload,
            payload_type
        }
    }
}