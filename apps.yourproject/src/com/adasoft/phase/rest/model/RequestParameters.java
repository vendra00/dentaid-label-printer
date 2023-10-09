package com.adasoft.phase.rest.model;

public class RequestParameters {

	private String body;
	private String token;

	public RequestParameters(String body, String token) {
		this.body = body;
		this.token = token;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
