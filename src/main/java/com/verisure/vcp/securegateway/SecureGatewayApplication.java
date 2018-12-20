package com.verisure.vcp.securegateway;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import com.verisure.vcp.securegateway.web.RestTemplateResponseErrorHandler;

/**
 * Secured Gateway application: for mutual SSL with client and OAuth2 with the backend
 * Enable and require client authentication by certificate
 * Establish an Oauth2 authentication (client-credentials grant) with the backend
 * 
 * Allows to configure Non Host Name Verification in SSL handshake with the backend, only for DEV environments
 * 
 * Adds a response wrapper to allow read the response more than one time. It allows to add Error handler that reads the response
 * 
 * The response handler returns the same backend error to the client
 * 
 * @author miguel.salas
 *
 */

@SpringBootApplication
public class SecureGatewayApplication {

	@Value("${security.oauth2.client.access-token-uri}")
	private String tokenUrl;

	@Value("${security.oauth2.client.client-id}")
	private String clientId;

	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;

	@Value("${http.client.backend.ssl-host-name-verification}")
	private boolean sslHostNameVerification;

	@Value("${server.ssl.trust-store}")
	private Resource truststore;

	@Value("${server.ssl.trust-store-password}")
	private String truststorePassword;

	public static void main(String[] args) {
		SpringApplication.run(SecureGatewayApplication.class, args);
	}

	@Bean
	OAuth2RestTemplate oauth2RestTemplate() throws Exception {
		OAuth2ProtectedResourceDetails resource = getClientCredentialsResource();
		OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
		HttpComponentsClientHttpRequestFactory sslRequestFactory = getSSLRequestFactory(); 
		addRequestResponseWrapper(oAuth2RestTemplate, sslRequestFactory);
		setClientCredentialsAccessTokenProvider(oAuth2RestTemplate, sslRequestFactory);	
		oAuth2RestTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
		return oAuth2RestTemplate;
	}

	private ClientCredentialsResourceDetails getClientCredentialsResource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(tokenUrl);
		resource.setClientId(clientId);
		resource.setClientSecret(clientSecret);
		return resource;
	}
	
	private void addRequestResponseWrapper(OAuth2RestTemplate oAuth2RestTemplate, HttpComponentsClientHttpRequestFactory requestFactory){
		oAuth2RestTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
	}

	private void setClientCredentialsAccessTokenProvider(OAuth2RestTemplate oAuth2RestTemplate, HttpComponentsClientHttpRequestFactory requestFactory)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
			IOException {
		ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider();
		provider.setRequestFactory(requestFactory);
		oAuth2RestTemplate.setAccessTokenProvider(provider);		
	}

	private HttpComponentsClientHttpRequestFactory getSSLRequestFactory() throws KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		CloseableHttpClient httpClient;
		HostnameVerifier hostNameVerifier;
		SSLContext sslContext;
		SSLConnectionSocketFactory socketFactory;
		HttpComponentsClientHttpRequestFactory requestFactory;

		if (sslHostNameVerification) {
			hostNameVerifier = new DefaultHostnameVerifier();
		} else {
			hostNameVerifier = new NoopHostnameVerifier();
		}

		sslContext = new SSLContextBuilder().loadTrustMaterial(truststore.getURL(), truststorePassword.toCharArray())
				.build();
		socketFactory = new SSLConnectionSocketFactory(sslContext, hostNameVerifier);
		httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
		requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		return requestFactory;
	}
}
