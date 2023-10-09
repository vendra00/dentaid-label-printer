package com.adasoft.phase.rest.model;

public enum RequestType {
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");

	private final String method;

	RequestType(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	// Helper method to convert a string to the corresponding RequestType
	public static RequestType fromString(String method) {
		for (RequestType type : RequestType.values()) {
			if (type.method.equalsIgnoreCase(method)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid RequestType: " + method);
	}

}

