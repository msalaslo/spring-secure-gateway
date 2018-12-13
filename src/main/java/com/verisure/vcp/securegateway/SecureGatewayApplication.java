package com.verisure.vcp.securegateway;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

@SpringBootApplication
public class SecureGatewayApplication {

	private static final Logger logger = LoggerFactory.getLogger(SecureGatewayApplication.class);

	@Value("${security.oauth2.client.access-token-uri}")
	private String tokenUrl;

	@Value("${security.oauth2.client.client-id}")
	private String clientId;

	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;

	@Value("${http.client.backend.ssl-host-name-verification}")
	private boolean sslHostNameVerification;

	public static void main(String[] args) {
		SpringApplication.run(SecureGatewayApplication.class, args);
			
	}
	
	@PostConstruct
	public void initSsl(){
//		System.setProperty("javax.net.ssl.trustStore", Thread.currentThread().getContextClassLoader().getResource("server-truststore.jks").getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "secret");
		
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
			(hostname,sslSession) -> {
				if (hostname.equals("localhost") || hostname.startsWith("es1pocmom01v")) {
					return true;
				}
				return false;
			});
	}

	@Bean
	OAuth2RestTemplate oauth2RestTemplate() throws Exception {
		OAuth2ProtectedResourceDetails resource = getClientCredentialsResource();
		OAuth2RestTemplate oauth2RestTemplate = new OAuth2RestTemplate(resource);
		// oauth2RestTemplate.setInterceptors(Collections.singletonList(new
		// RequestResponseLoggingInterceptor()));
		if (!sslHostNameVerification) {
			setNoVerifyHostNameInSSL(oauth2RestTemplate);
		}
		return oauth2RestTemplate;
	}

	private ClientCredentialsResourceDetails getClientCredentialsResource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(tokenUrl);
		resource.setClientId(clientId);
		resource.setClientSecret(clientSecret);
		return resource;
	}

	private static void setNoVerifyHostNameInSSL(OAuth2RestTemplate oAuth2RestTemplate)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		logger.info("Setting no name host verification in SSL handshake");
		// ClientHttpRequestFactory requestFactory = new
		// SSLContextRequestNoHostnameVerifierFactory();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory();
		oAuth2RestTemplate.setRequestFactory(requestFactory);
		ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider();
		provider.setRequestFactory(requestFactory);
		oAuth2RestTemplate.setAccessTokenProvider(provider);
	}

	private static HttpComponentsClientHttpRequestFactory getRequestFactory()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
				.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		return requestFactory;
	}
}
