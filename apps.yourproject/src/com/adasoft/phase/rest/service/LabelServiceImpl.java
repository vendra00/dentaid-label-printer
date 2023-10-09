package com.adasoft.phase.rest.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.adasoft.phase.rest.model.AbstractLabelService;
import com.adasoft.phase.rest.model.Label;
import com.adasoft.phase.rest.model.LogMsgDialect;
import com.adasoft.phase.rest.model.Request;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class LabelServiceImpl extends AbstractLabelService implements LabelService {

	private static final Logger LOGGER = Logger.getLogger(LabelServiceImpl.class.getName());

	@Override
	public List<Label> fetchAllLabelsFromServer(Request request) {
		LOGGER.info(LogMsgDialect.FETCH_ALL_LABELS_FROM_SERVER.getMsgDialect());
		String serverUrl = buildUrl(request);
		List<Label> labels = new ArrayList<>();

		try {
			HttpGet httpGet = new HttpGet(serverUrl);

			// Execute the request
			HttpResponse response = executeRequest(httpGet);

			// Handle the response
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				StringBuilder responseText = readResponse(responseEntity);

				// Process the responseText as JSON using Jackson library
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonResponse = objectMapper.readTree(responseText.toString());
				// Check if the response contains an array of labels
				if (jsonResponse.isArray()) {
					ArrayNode labelsArray = (ArrayNode) jsonResponse;
					for (JsonNode labelNode : labelsArray) {
						// Extract label attributes from each label node
						String name = labelNode.get("name").asText();
						String description = labelNode.get("description").asText();
						float value = labelNode.get("value").floatValue();

						// Create a new Label object and add it to the list
						Label label = new Label(name, description, value);
						labels.add(label);
					}
				}	            
			}

			if (statusCode == 200) {
				LOGGER.info(LogMsgDialect.FETCH_ALL_LABELS_FROM_SERVER_OK.getMsgDialect() + response.getStatusLine().getReasonPhrase());
				return labels;
			} else {
				LOGGER.info(LogMsgDialect.FETCH_ALL_LABELS_FROM_SERVER_KO.getMsgDialect() + response.getStatusLine().getReasonPhrase());
				return labels;
			}
		} catch (IOException e) {
			LOGGER.severe(LogMsgDialect.FETCH_ALL_LABELS_FROM_SERVER_ERROR.getMsgDialect() + e.getMessage());
			e.printStackTrace();
			return labels;
		}
	}

	@Override
	public boolean pingServer(Request request) {
		LOGGER.info(LogMsgDialect.PING_SERVER_CALL.getMsgDialect());
		String serverUrl = buildUrl(request);

		HttpGet httpGet = new HttpGet(serverUrl);

		try {
			HttpResponse response = executeRequest(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 200) {				
				LOGGER.info(LogMsgDialect.PING_OK.getMsgDialect());
				return true;
			} else {
				LOGGER.info(LogMsgDialect.PING_KO.getMsgDialect() + statusCode);				
				return false;
			}
		} catch (IOException e) {
			LOGGER.severe(LogMsgDialect.PING_SERVER_ERROR + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean createLabelInServer(Request request) {
		LOGGER.info(LogMsgDialect.CREATE_LABEL_IN_SERVER_CALL.getMsgDialect());
		String serverUrl = buildUrl(request);

		try {
			HttpPost httpPost = new HttpPost(serverUrl);

			// Create a JSON payload with the label attributes
			String jsonPayload = request.getParameters().getBody();
			StringEntity entity = new StringEntity(jsonPayload);
			httpPost.setEntity(entity);
			httpPost.setHeader("Content-Type", "application/json");

			// Execute the request
			HttpResponse response = executeRequest(httpPost);

			// Handle the response
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				StringBuilder responseText = readResponse(responseEntity);				
				LOGGER.info(LogMsgDialect.CREATE_LABEL_RESPONSE_FROM_SERVER.getMsgDialect() + responseText.toString());
			}

			if (statusCode == 200) {
				LOGGER.info(LogMsgDialect.CREATE_LABEL_OK.getMsgDialect());
				return true;
			} else {
				LOGGER.info(LogMsgDialect.CREATE_LABEL_KO.getMsgDialect() + response.getStatusLine().getReasonPhrase());
				return false;
			}
		} catch (IOException e) {
			LOGGER.severe(LogMsgDialect.CREATE_LABEL_ERROR.getMsgDialect() + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	

	@Override
	public boolean editLabelInServer(Request request) {
		LOGGER.info(LogMsgDialect.EDIT_LABEL_IN_SERVER_CALL.getMsgDialect());
	    String serverUrl = buildUrl(request);
	    String jsonString = request.getParameters().getBody();
	    String completeUrl = buildCompleteUrl(serverUrl, jsonString);
	    
	    if (completeUrl == null) return false;

	    try {
	    	HttpPut httpPut = new HttpPut(completeUrl);

	        // Create a JSON payload with the label attributes
	        String jsonPayload = request.getParameters().getBody();
	        StringEntity entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
	        httpPut.setEntity(entity);
	        httpPut.setHeader("Content-Type", "application/json");

	        // Execute the request
	        HttpResponse response = executeRequest(httpPut);

	        // Handle the response
	        int statusCode = response.getStatusLine().getStatusCode();
	        HttpEntity responseEntity = response.getEntity();

	        if (responseEntity != null) {
	            StringBuilder responseText = readResponse(responseEntity);
	            LOGGER.info(LogMsgDialect.EDIT_LABEL_RESPONSE_FROM_SERVER.getMsgDialect() + responseText.toString());
	        }

	        if (statusCode == 200) {
	            LOGGER.info(LogMsgDialect.EDIT_LABEL_OK.getMsgDialect());
	            return true;
	        } else {
	            LOGGER.warning(LogMsgDialect.EDIT_LABEL_KO.getMsgDialect() + response.getStatusLine().getReasonPhrase());
	            return false;
	        }
	    } catch (Exception e) {
	        LOGGER.severe(LogMsgDialect.EDIT_LABEL_ERROR.getMsgDialect() + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}

	

	@Override
	public boolean deleteLabelInServer(Request request) {
		LOGGER.info(LogMsgDialect.DELETE_LABEL_IN_SERVER_CALL.getMsgDialect());
	    String serverUrl = buildUrl(request);
	    String jsonString = request.getParameters().getBody();
	    String completeUrl = buildCompleteUrl(serverUrl, jsonString);
	    
	    if (completeUrl == null) return false;

	    try {
	        HttpDelete httpDelete = new HttpDelete(completeUrl);
	        HttpResponse response = executeRequest(httpDelete);

	        // Handle the response
	        int statusCode = response.getStatusLine().getStatusCode();

	        if (statusCode == 204) {
	            LOGGER.info(LogMsgDialect.DELETE_LABEL_OK.getMsgDialect());
	            return true;
	        } else {
	            LOGGER.warning(LogMsgDialect.DELETE_LABEL_KO.getMsgDialect() + response.getStatusLine().getReasonPhrase());
	            return false;
	        }
	    } catch (Exception e) {
	        LOGGER.severe(LogMsgDialect.DELETE_LABEL_ERROR.getMsgDialect() + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
	
	private String buildCompleteUrl(String serverUrl, String jsonString) {
	    String name;
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode = objectMapper.readTree(jsonString);
	        name = jsonNode.get("nameUpdate").asText();
	    } catch (IOException e) {
	        LOGGER.severe(LogMsgDialect.PARSING_JSON_ERROR.getMsgDialect() + e.getMessage());
	        e.printStackTrace();
	        return null;
	    }

	    try {
	        return serverUrl + "?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8.name());
	    } catch (UnsupportedEncodingException e) {
	        LOGGER.severe(LogMsgDialect.ENCONDING_URL_ERROR.getMsgDialect() + e.getMessage());
	        e.printStackTrace();
	        return null;
	    }
	}



}
