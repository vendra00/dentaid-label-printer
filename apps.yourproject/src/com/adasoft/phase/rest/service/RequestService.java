package com.adasoft.phase.rest.service;

import java.util.HashMap;
import java.util.List;

import com.adasoft.phase.rest.model.Request;

public interface RequestService {
	Request createRequest(Long id, String method, String url, HashMap<String, String> body);
	List<Request> getRequests();
}
