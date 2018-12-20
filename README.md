# Secure Gateway supporting mutual SSL at the frontend and OAuth2 (client credentials grant)  at the backend.

## Mutual SSL authentication
 Enables and requires client authentication by certificate: the application incorporates an internal truststore to add the trusted certificates and an internal keystore to add the server certificate.
 
## OAuth2 Authentication
 Establish an Oauth2 authentication (client-credentials grant) with the backend. HTTPS/SSL channel is supported, you can add the backend server certificates and activate Non host name verification in SSL handshake for develop environments with self-signed certificates. 
 
### Error handling 
Adds a response handler interceptor that returns the same backend error to the client. In this scenario where we need to read the request/response stream twice, the first time by the interceptor and the second time by the client. The default implementation allows us to read the response stream only once. To cater such specific scenarios, Spring provides a special class called BufferingClientHttpRequestFactory. As the name suggests, this class will buffer the request/response in JVM memory for multiple usage.

Here’s how the RestTemplate object is initialized using BufferingClientHttpRequestFactory  to enable the request/response stream caching:
```java
RestTemplate restTemplate 
  = new RestTemplate(
    new BufferingClientHttpRequestFactory(
      new SimpleClientHttpRequestFactory()
    )
  );
 ```
 

