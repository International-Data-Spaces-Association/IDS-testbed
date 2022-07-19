#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct DocumentType {
    pub id: String,
    pub pid: String,
    pub parts: Vec<DocumentTypePart>,
}

impl DocumentType {
    pub fn new(id: String, pid: String, parts: Vec<DocumentTypePart>) -> DocumentType {
        DocumentType{
            id,
            pid,
            parts,
        }
    }
}

#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct DocumentTypePart {
    pub name: String,
}

impl DocumentTypePart {
    pub fn new(name: String) -> DocumentTypePart{
        DocumentTypePart{
            name
        }
    }
}
