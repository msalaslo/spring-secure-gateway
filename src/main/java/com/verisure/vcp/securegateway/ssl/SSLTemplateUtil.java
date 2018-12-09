package com.verisure.vcp.securegateway.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;

public class SSLTemplateUtil {
	
	public static void setNoVerifyHostNameInSSL(OAuth2RestTemplate oAuth2RestTemplate) {
		// request factory
		ClientHttpRequestFactory requestFactory;
		ClientCredentialsAccessTokenProvider provider;
		try {
			requestFactory = new SSLContextRequestNoHostnameVerifierFactory();
			oAuth2RestTemplate.setRequestFactory(requestFactory);
			// provider
			provider = new ClientCredentialsAccessTokenProvider();
			provider.setRequestFactory(requestFactory);
			oAuth2RestTemplate.setAccessTokenProvider(provider);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
