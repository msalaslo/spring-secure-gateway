package com.verisure.vcp.securegateway;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import com.verisure.vcp.securegateway.ssl.SSLContextRequestNoHostnameVerifierFactory;

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

	public static void main(String[] args) {
		SpringApplication.run(SecureGatewayApplication.class, args);
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
		//ClientHttpRequestFactory requestFactory = new SSLContextRequestNoHostnameVerifierFactory();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory();
		oAuth2RestTemplate.setRequestFactory(requestFactory);
		ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider();
		provider.setRequestFactory(requestFactory);
		oAuth2RestTemplate.setAccessTokenProvider(provider);
	}

	private static HttpComponentsClientHttpRequestFactory getRequestFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		return requestFactory;
	}
}
