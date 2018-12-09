package com.verisure.vcp.securegateway;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SecureGatewayApplication {

	@Value("${server.ssl.trust-store}")
	private Resource trustStoreFile;

	@Value("${server.ssl.trust-store-password}")
	private String trustStorePassword;

	@Value("${security.oauth2.client.access-token-uri}")
    private String tokenUrl;
	
	@Value("${security.oauth2.client.client-id}")
    private String clientId;
	
	@Value("${security.oauth2.client.client-secret}")
    private String clientSecret;
	
	@Value("${security.oauth2.client.grant-type}")
    private String grantType;
	
	@Value("${security.oauth2.client.user-name}")
    private  String userName;
	
	@Value("${security.oauth2.client.user-password}")
    private String userPassword;

	public static void main(String[] args) {
		SpringApplication.run(SecureGatewayApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() throws Exception {
		SSLContext sslContext = new SSLContextBuilder()
				.loadTrustMaterial(trustStoreFile.getURL(), trustStorePassword.toCharArray()).build();
		// TODO: Remove when we have a CA Signed certificate in the backend
		HttpComponentsClientHttpRequestFactory factory = getNoHostnameVerifierFactory(sslContext);
		return new RestTemplate(factory);
	}
	
	@Bean
	OAuth2RestTemplate oauth2RestTemplate() throws Exception {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId(clientId);
        resource.setClientSecret(clientSecret);
        resource.setGrantType(grantType);
        resource.setUsername(userName);
        resource.setPassword(userPassword);		
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
        // TODO: Remove when we have a CA Signed certificate in the backend
        this.setNoSSL(oAuth2RestTemplate); //to ignore ssl
        return oAuth2RestTemplate;
	}

	private HttpComponentsClientHttpRequestFactory getNoHostnameVerifierFactory(SSLContext sslContext) {
		HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return factory;
	}
	
    private void setNoSSL(OAuth2RestTemplate oAuth2RestTemplate) throws KeyManagementException, NoSuchAlgorithmException {
        //request factory
        ClientHttpRequestFactory requestFactory = new SSLContextRequestFactory();
        oAuth2RestTemplate.setRequestFactory(requestFactory);
        //provider
        ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
        provider.setRequestFactory(requestFactory);
        oAuth2RestTemplate.setAccessTokenProvider(provider);
    }
}
