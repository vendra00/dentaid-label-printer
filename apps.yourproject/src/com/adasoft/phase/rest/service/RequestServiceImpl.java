package com.adasoft.phase.rest.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.adasoft.phase.rest.model.LogMsgDialect;
import com.adasoft.phase.rest.model.Request;
import com.adasoft.phase.rest.model.RequestParameters;
import com.adasoft.phase.rest.model.RequestType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestServiceImpl implements RequestService {
	
	private static final Logger LOGGER = Logger.getLogger(RequestServiceImpl.class.getName());

	private static final List<Request> requests = new ArrayList<>();

	@Override
	public Request createRequest(Long id, String method, String url, HashMap<String, String> body) {
		LOGGER.info(LogMsgDialect.CREATE_REQUEST_CALL.getMsgDialect());
		RequestType requestType = RequestType.fromString(method);
		String token = null;    
		RequestParameters requestParameters = new RequestParameters(convertBodyToJSON(body), token);
		LOGGER.info(LogMsgDialect.PARSED_BODY_TO_JSON.getMsgDialect() + requestParameters.getBody());
		Request request = new Request(id, requestType, url, requestParameters);      
		requests.add(request); 
		return request;
	}

	@Override
	public List<Request> getRequests() {
		return Collections.unmodifiableList(requests);
	}

	private String convertBodyToJSON(HashMap<String, String> body) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(body);
		} catch (Exception e) {
			LOGGER.severe(LogMsgDialect.CONVERT_BODY_TO_JSON_ERROR.getMsgDialect() + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
