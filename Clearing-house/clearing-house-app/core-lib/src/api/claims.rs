use biscuit::CompactJson;

#[derive(Debug, PartialEq, Clone, Serialize, Deserialize)]
pub struct IdsClaims {
    /// Ids scope supports DAPSv3 tokens
    #[serde(skip_serializing_if = "Option::is_none")]
    pub scope: Option<String>,
    /// Ids scopes supports DAPSv2 tokens
    #[serde(skip_serializing_if = "Option::is_none")]
    pub scopes: Option<Vec<String>>,
    #[serde(rename = "securityProfile")]
    pub security_profile: String,
    #[serde(rename = "@type")]
    pub claim_type: String,
    #[serde(rename = "@context")]
    pub claim_context: String,
    #[serde(rename = "transportCertsSha256")]
    pub transport_certs_sha256: String,
}
impl CompactJson for IdsClaims {
}