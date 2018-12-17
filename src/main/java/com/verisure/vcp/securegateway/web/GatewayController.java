package com.verisure.vcp.securegateway.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.verisure.vcp.securegateway.client.BackendClientService;

@RestController
public class GatewayController {

	@Autowired
	private BackendClientService service;

	@GetMapping("/test")
	public String home(Principal principal) {
		return String.format("Hello %s!", principal.getName());
	}

	@GetMapping(value = "/**")
	public ResponseEntity<String> getGateway(HttpServletRequest request, Principal principal)
			throws RestClientException {
		return service.get(request, principal);
	}
	
	@PatchMapping(value = "/**")
	public Object patchGateway(HttpServletRequest request, @RequestBody String json, Principal principal)
			throws RestClientException {
		return service.patch(request, json, principal);
	}
	
	@RequestMapping(value = "/**")
	public ResponseEntity<String> postGateway(HttpServletRequest request, @RequestBody String json, Principal principal)
			throws RestClientException {
		return service.post(request, json, principal);
	}

	@DeleteMapping(value = "/**")
	public ResponseEntity<String> deleteGateway(HttpServletRequest request, Principal principal)
			throws RestClientException {
		return service.get(request, principal);
	}

	@PutMapping(value = "/**")
	public ResponseEntity<String> putGateway(HttpServletRequest request, @RequestBody String json, Principal principal)
			throws RestClientException {
		return service.post(request, json, principal);
	}
}
