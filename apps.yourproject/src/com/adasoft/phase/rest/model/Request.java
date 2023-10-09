package com.adasoft.phase.rest.model;

public class Request {

	private Long id;
	private RequestType requestType;
	private String url;
	private RequestParameters parameters;

	public Request(Long id, RequestType requestType, String url, RequestParameters parameters) {
		this.id = id;
		this.requestType = requestType;
		this.url = url;
		this.parameters = parameters;
	}

	public Request() {   
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public RequestParameters getParameters() {
		return parameters;
	}

	public void setParameters(RequestParameters parameters) {
		this.parameters = parameters;
	}	

}
