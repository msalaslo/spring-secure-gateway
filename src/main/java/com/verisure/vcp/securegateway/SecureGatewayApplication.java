package com.verisure.vcp.securegateway;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.util.CollectionUtils;

import com.verisure.vcp.securegateway.util.ResponseWrapperInterceptor;
import com.verisure.vcp.securegateway.web.RestTemplateResponseErrorHandler;

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
		OAuth2RestTemplate oauth2RestTemplate = new OAuth2RestTemplate(resource);
		setClientCredentialsAccessTokenProvider(oauth2RestTemplate);

		List<ClientHttpRequestInterceptor> interceptors = oauth2RestTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) {
			interceptors = new ArrayList<>();
		}
	
		interceptors.add(new ResponseWrapperInterceptor());
		
		oauth2RestTemplate.setInterceptors(interceptors);
		oauth2RestTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
		return oauth2RestTemplate;
	}

	private ClientCredentialsResourceDetails getClientCredentialsResource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(tokenUrl);
		resource.setClientId(clientId);
		resource.setClientSecret(clientSecret);
		return resource;
	}

	private void setClientCredentialsAccessTokenProvider(OAuth2RestTemplate oAuth2RestTemplate)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
			IOException {
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory();
		oAuth2RestTemplate.setRequestFactory(requestFactory);
		ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider();
		provider.setRequestFactory(requestFactory);
		oAuth2RestTemplate.setAccessTokenProvider(provider);
	}

	private HttpComponentsClientHttpRequestFactory getRequestFactory() throws KeyManagementException,
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
