package com.adasoft.phase.label_printer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.adasoft.phase.label_printer.model.Request;

public class LabelPrinterServiceImpl implements LabelPrinterService{
	
	private static final Logger LOGGER = Logger.getLogger(LabelPrinterServiceImpl.class.getName());

	@Override
	public boolean sendlabelToPrint(Request request) {
	    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
	        HttpPost httpPost = new HttpPost(request.getUrl());

	        // Create a JSON payload with the label attributes
	        String jsonPayload = request.getParameters().getBody();
	        StringEntity entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
	        httpPost.setEntity(entity);
	        httpPost.setHeader("Content-Type", "application/json");
	        httpPost.setHeader("Authorization", "Bearer " + request.getParameters().getApiKey());

	        CloseableHttpResponse response = httpClient.execute(httpPost);
	        
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != 200) {  
	            LOGGER.severe("Error executing request. Received status code: " + statusCode);
	            EntityUtils.consumeQuietly(response.getEntity());  
	            return false;
	        }

	        EntityUtils.consumeQuietly(response.getEntity());  
	        return true;

	    } catch (IOException e) {
	        LOGGER.severe("A problem occurred when executing the endpoint call: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}



}
