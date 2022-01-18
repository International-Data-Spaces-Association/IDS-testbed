

# API

## Clients

### List clients

**GET** */api/v1/config/clients*

Request payload: empty  
Success response: ```200 OK```  
Response payload:
```
[
  {
    "client_id": String,
    "name": String,
    "scopes": Array<String>,
    "redirect_uri": String,
    "user_attributes": Array<String>,
    "attributes": Array<String>
  },
  ...
]
```

### Show client

**GET** */api/v1/config/clients/:client_id*

Request payload: empty  
Success response: ```200 OK```  
Response payload:
```
{
  "client_id": String,
  "name": String,
  "scopes": Array<String>,
  "redirect_uri": String,
  "user_attributes": Array<String>,
  "attributes": Array<String>
}
```

### Update client

**PUT** */api/v1/config/clients/:client_id*

Request payload:
```
{
  "client_id": String,
  "name": String,
  "scopes": Array<String>,
  "redirect_uri": String,
  "user_attributes": Array<String>,
  "attributes": Array<String>
}
```

Success response: ```200 OK```  
Response payload: empty

### Add client

**POST** */api/v1/config/clients/:client_id*

Request payload:
```
{
  "client_id": String,
  "name": String,
  "scopes": Array<String>,
  "redirect_uri": String,
  "user_attributes": Array<String>,
  "attributes": Array<String>
}
```
Success response: ```200 OK```  
Response payload: empty  


### Delete client

**DELETE** */api/v1/config/clients/:client_id*

Request payload: empty   
Success response: ```200 OK ```   
Response payload: empty   


### Get a clients certificate

**GET** */api/v1/config/clients/keys/:client_id*

Request payload: empty    
Success respnse: ```200 OK```    
Response payload: 

```
{
  "certificate": String
}
```


### Update a clients certificate

**PUT** */api/v1/config/clients/keys/:client_id*

Request payload: 

```
{
  "certificate": String
}
```
Success response: ```200 OK ```     
Response payload: empty     



### Add a clients certificate

**POST** */api/v1/config/clients/keys/:client_id*

Request payload: 

```
{
  "certificate": String
}
```
Success response: ```200 OK ```     
Response payload: empty     



### Delete a clients certificate

**DELETE** */api/v1/config/clients/keys/:client_id*

Request payload: empty   
Success response: ```200 OK ```   
Response payload: empty  


## Users


### List users

**GET** */api/v1/config/users*

Request payload: empty    
Success response: ```200 OK```     
Response payload    
```
[
  {
    "username": String,
    "scopes": Array <String>,
    "attributes": Array <Attribute>,
    "password": String
  },
   ...
]
```

Attribute:
```
{
  key: String,
  value: any
}
```

### Show user

**GET***'/api/v1/config/users/:username*

Request payload: empty    
Success response: ```200 OK```    
Response payload    

```
{
  "username": String,
  "scopes": Array <String>,
  "attributes": Array <Attribute>,
  "password": String
}
```

Attribute:
```
{
  key: String,
  value: any
}
```


### Add user

**POST** */api/v1/config/users/:username*

Request payload

```
{
  "username": String,
  "scopes": Array <String>,
  "attributes": Array <Attribute>,
  "password": String
}
```

Attribute:
```
{
  key: String,
  value: any
}
```
Success response: ```200 OK ```    
Response payload: empty    


### Update user

**PUT** */api/v1/config/users/:username*

Request payload

```
{
  "username": String,
  "scopes": Array <String>,
  "attributes": Array <Attribute>,
  "password": String
}
```

Attribute:
```
{
  key: String,
  value: any
}
```
Success response: ```200 OK ```    
Response payload: empty    


### Delete user

**DELETE** */api/v1/config/users/:username*

Request payload: empty    
Success response: ```200 OK ```    
Response payload: empty    



## Config

### Show config

**GET** */api/v1/config/omejdn*

Request payload: empty    
Success response: ```200 OK ```    
Response payload:    

```
{
  "host": String,
  "openid": boolean,
  "token":
    {
      "expiration": number,
      "signing_key": String,
      "algorithm": String,
      "audience": String,
      "issuer": String
    },
  "id_token":
    {
      "expiration": number,
      "signing_key": String,
      "algorithm": String,
      "issuer": String
    }
  }
```


### Update config

**PUT** */api/v1/config/omejdn*

Request payload:

```
{
  "host": String,
  "openid": boolean,
  "token":
    {
      "expiration": number,
      "signing_key": String,
      "algorithm": String,
      "audience": String,
      "issuer": String
    },
  "id_token":
    {
      "expiration": number,
      "signing_key": String,
      "algorithm": String,
      "issuer": String
    }
  }
```
Success response: ```200 OK ```    
Response payload: empty   

