package com.verisure.vcp.securegateway.client;

public class BackendClientException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public BackendClientException(Exception e) {
		super(e);
	}

}
