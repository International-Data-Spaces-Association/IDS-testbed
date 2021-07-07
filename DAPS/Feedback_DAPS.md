# Review - DAPS 

[Link to Specifications](https://github.com/International-Data-Spaces-Association/omejdn-daps)

## Feedback 

1. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps#spec

To own the directory /opt with "config" and "Keys" is not desirable.

Suggestion for improvement
```
/opt/omejdn/config
/opt/omejdn/keys
```

2. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps#spec 

*and mount them using docker*

Is this a good idea? This can very quickly lead to security problems.

3. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps#configuring-the-server

It is unclear what to do here. How must the environment variables be set?

Persistent Environment Variables?
Please add the commands
```
export APP_ENV="/path/to/"
```

4. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps#configuring-the-server

There are no explanation available, what's the difference between production and debug.

5. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps 

*First, you need to create a public/private RSA key pair and a X.509 certificate.*

There are no instructions on how to create the keys.
There is no RSA key size requirement.

6. Topic

*Now you need to add your client clientID to the config file config/clients.yml:*

What is a clientID? Number, String ? Must be unique?  

7. Topic

*Adding a user*

The command to generate the password should be added.

8. Topic

*Note: You need to generate the respective private key yourself.*

The command to generate the private key should be added.

9. Topic

*You may use the script create_test_token.rb to generate a JWT Bearer token with your private key.*

The create_test_token.rb script is not in the repository.

10. Topic
```
curl localhost:4567/token --data
```
Please explain the Parameters.

11. Topic

*Transport Layer Security*

Instructions for setting up TLS are required.

12. Topic

https://github.com/International-Data-Spaces-Association/omejdn-daps/blob/master/LICENSE

The License file is given "Apache License 2.0", thats is fine.

13. Topic

Is there a list of further software components? 

14. Topic

There ist no SBOM (https://cyclonedx.org/use-cases/) available?
