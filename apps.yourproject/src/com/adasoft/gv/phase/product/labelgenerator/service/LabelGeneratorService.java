package com.adasoft.gv.phase.product.labelgenerator.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;



import com.adasoft.gv.phase.product.labelgenerator.Label;


public class LabelGeneratorService {
	
	private final String baseUrl = "http://192.168.15.15:8080/adasoft/api/labels";
	
	public boolean sendLabelAttributesToServer(String name, String description, float value) {
		
		String serverUrl = baseUrl + "/create-label";
        
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(serverUrl);

            // Create a JSON payload with the label attributes
            String jsonPayload = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"value\":%f}", name, description, value);
            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Handle the response
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder responseText = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                    // Process the responseText as needed
                    System.out.println("Response from server: " + responseText.toString());
                }
            }

            if (statusCode == 200) {
            	System.out.println("Label created successfully!");
            	return true;
            } else {
            	System.err.println("Label creation failed: " + response.getStatusLine().getReasonPhrase());
                // Request failed, handle the error
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public List<Label> fetchAllLabelsFromServer() {
		
		String serverUrl = baseUrl + "/get-all-labels";
		
        List<Label> labels = new ArrayList<>();
                    
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(serverUrl);

            // Execute the request
            HttpResponse response = httpClient.execute(httpGet);

            // Handle the response
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder responseText = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
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
            }

            if (statusCode == 200) {
                // Request successful
                return labels;
            } else {
                // Request failed, handle the error
                System.err.println("Failed to fetch labels from server: " + response.getStatusLine().getReasonPhrase());
                return labels;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return labels;
        }
    }

	public boolean updateLabelOnServer(String originalName, Label updatedLabel) {
	    try {
	        // Encode the label name to handle spaces and special characters
	    	// Encode the original name to handle spaces and special characters correctly
            String encodedName = urlEncode(originalName);
	        String serverUrl = baseUrl + "/update-by-name?name=" + encodedName;

	        HttpClient httpClient = HttpClientBuilder.create().build();
	        HttpPut httpPut = new HttpPut(serverUrl);

	        // Create a JSON payload with the updated label attributes
	        ObjectMapper objectMapper = new ObjectMapper();
	        String jsonPayload = objectMapper.writeValueAsString(updatedLabel);
	        StringEntity entity = new StringEntity(jsonPayload);
	        httpPut.setEntity(entity);
	        httpPut.setHeader("Content-Type", "application/json");

	        // Execute the request
	        HttpResponse response = httpClient.execute(httpPut);

	        // Handle the response
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode == 200) {
	            // Label updated successfully
	            return true;
	        } else {
	            // Request failed, handle the error
	            System.err.println("Failed to update label on server: " + response.getStatusLine().getReasonPhrase());
	            return false;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public boolean deleteLabelOnServer(String name) {
	    try {
	        // Encode the label name to handle spaces and special characters correctly
	        String encodedName = urlEncode(name);
	        String serverUrl = baseUrl + "/delete-label-by-name?name=" + encodedName;

	        HttpClient httpClient = HttpClientBuilder.create().build();
	        HttpDelete httpDelete = new HttpDelete(serverUrl);

	        // Execute the request
	        HttpResponse response = httpClient.execute(httpDelete);

	        // Handle the response
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode == 200) {
	            // Label deleted successfully
	            return true;
	        } else {
	            // Request failed, handle the error
	            System.err.println("Failed to delete label on server: " + response.getStatusLine().getReasonPhrase());
	            return false;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	private String urlEncode(String value) throws IOException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
    }

}
