package com.verisure.vcp.securegateway.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class GatewayController {
	
	@Value( "${http.client.backend.protocol}" )
	private String backendProtocol;
	
	@Value( "${http.client.backend.host}" )
	private String backendHost;
	
	@Value( "${http.client.backend.port}" )
	private String backendPort;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private OAuth2RestTemplate oauth2RestTemplate;

	@GetMapping("/")
	public String home(Principal principal) {
		return String.format("Hello %s!", principal.getName());
	}

	@RequestMapping(value = "/config/**")
	public String gateway(HttpServletRequest request) throws RestClientException, URISyntaxException{
		return restTemplate.getForObject(new URI(getBackendUrl() + request.getRequestURI()), String.class);
	}
	
	@RequestMapping(value = "/pizzashack/**")
	public String secure(HttpServletRequest request) throws RestClientException, URISyntaxException{
		return oauth2RestTemplate.getForObject(new URI(getBackendUrl() + request.getRequestURI()), String.class);
	}
	
	private String getBackendUrl() {
		return backendProtocol + "://" + backendHost + ":" + backendPort;
	}
}
