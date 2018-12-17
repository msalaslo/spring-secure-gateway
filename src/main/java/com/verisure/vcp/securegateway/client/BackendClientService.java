package com.verisure.vcp.securegateway.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;

@Service
public class BackendClientService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${http.client.backend.protocol}")
	private String backendProtocol;

	@Value("${http.client.backend.host}")
	private String backendHost;

	@Value("${http.client.backend.port}")
	private String backendPort;

	@Autowired
	private OAuth2RestTemplate oauth2RestTemplate;

	@RequestMapping(value = "/**")
	public ResponseEntity<String> gateway(HttpServletRequest request, String body, Principal principal) {
		MultiValueMap<String, String> requestHeaders = extractHeaders(request);
		HttpEntity<String> requestEntity = new HttpEntity<>(body, requestHeaders);
		ResponseEntity<String> response = oauth2RestTemplate.postForEntity(getBackendUrl(request), requestEntity,
				String.class);
		String responseBody = response.getBody();
		if (responseBody == null) {
			responseBody = "";
		}
		return new ResponseEntity<String>(responseBody, response.getHeaders(), response.getStatusCode());

	}

	@RequestMapping(value = "/**")
	public Object patchGateway(HttpServletRequest request, String body, Principal principal) {
		MultiValueMap<String, String> requestHeaders = extractHeaders(request);
		HttpEntity<String> requestEntity = new HttpEntity<>(body, requestHeaders);
		return oauth2RestTemplate.patchForObject(getBackendUrl(request), requestEntity, String.class);
	}

	@RequestMapping(value = "/**")
	public ResponseEntity<String> gateway(HttpServletRequest request, Principal principal)
			throws RestClientException {
		return oauth2RestTemplate.getForEntity(getBackendUrl(request), String.class);
	}

	private MultiValueMap<String, String> extractHeaders(HttpServletRequest request) {
		MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<String, String>();
		Enumeration<String> headersNames = request.getHeaderNames();
		while (headersNames.hasMoreElements()) {
			String key = (String) headersNames.nextElement();
			String value = (String) request.getHeader(key);
			if (!key.equalsIgnoreCase("Authorization") && !key.equalsIgnoreCase("content-lenght")) {
				requestHeaders.add(key, value);
			}
		}
		return requestHeaders;
	}

	private URI getBackendUrl(HttpServletRequest request) throws RestClientException {
		try {
			return new URI(backendProtocol + "://" + backendHost + ":" + backendPort + request.getRequestURI());
		} catch (URISyntaxException e) {
			String msg = "Exception generating backend URL:";
			logger.error(msg + e.getMessage());
			throw new RestClientException(msg, e);
		}
	}

}
