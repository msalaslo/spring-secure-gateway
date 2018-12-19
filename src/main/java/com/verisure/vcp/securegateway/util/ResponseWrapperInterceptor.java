package com.verisure.vcp.securegateway.util;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;


/**
 * ClientHttpRequestInterceptor to wrap the response in a new object in order 
 * to allow invoke response.getBody several times
 * 
 */
@Component
public class ResponseWrapperInterceptor implements ClientHttpRequestInterceptor {
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {	
		ClientHttpResponse response = execution.execute(request, body);	
		/** create wrapper to be able to work with the body of the response */
		BufferingClientHttpResponseWrapper wrapper = new BufferingClientHttpResponseWrapper(response);
		return wrapper;
	}
}

