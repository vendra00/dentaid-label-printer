package com.adasoft.phase.label_printer.service;

import java.util.List;

import com.adasoft.phase.label_printer.model.Request;
import com.adasoft.phase.label_printer.model.RequestParameters;


public interface RequestService {
	Request createRequest(Long id, String method, String url, RequestParameters parameters);
	List<Request> getRequests();
}
