#[derive(Clone, Serialize, Deserialize, Debug)]
pub struct Process {
    pub id: String,
    pub owners: Vec<String>,
}

impl Process {
    pub fn new(id: String, owners: Vec<String>) -> Process {
        Process {
            id,
            owners
        }
    }
}