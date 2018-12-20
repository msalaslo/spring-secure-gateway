package com.verisure.vcp.securegateway.web;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Include this error handler in order to return the same error message returned by the back-end service to the client
 * 
 * @author miguel.salas
 *
 */

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
		boolean ret = (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
				|| httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
		return ret;
	}

	@Override
	public void handleError(ClientHttpResponse httpResponse) throws IOException {
		ResponseEntity.status(httpResponse.getRawStatusCode()).headers(httpResponse.getHeaders())
				.body(httpResponse.getBody());
		httpResponse.close();
	}
}