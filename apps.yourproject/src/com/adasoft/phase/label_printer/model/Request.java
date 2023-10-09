package com.adasoft.phase.label_printer.model;

import lombok.Data;

@Data
public class Request {
	private Long id;
	private RequestType requestType;
	private String url;
	private RequestParameters parameters;
}
