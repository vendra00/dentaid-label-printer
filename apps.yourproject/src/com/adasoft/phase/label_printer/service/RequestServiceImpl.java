package com.adasoft.phase.label_printer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.adasoft.phase.label_printer.model.Request;
import com.adasoft.phase.label_printer.model.RequestParameters;
import com.adasoft.phase.label_printer.model.RequestType;
import com.adasoft.phase.label_printer.service.RequestServiceImpl;

public class RequestServiceImpl implements RequestService{
	
	private static final List<Request> requests = new ArrayList<>();

	
	@Override
	public List<Request> getRequests() {
		return Collections.unmodifiableList(requests);
	}


	@Override
	public Request createRequest(Long id, String method, String url, RequestParameters parameters) {
		RequestType requestType = RequestType.fromString(method);   
		Request request = new Request();
		request.setId(id);
		request.setUrl(url);
		request.setRequestType(requestType);
		request.setParameters(parameters);
		requests.add(request); 
		return request;
	}

}
