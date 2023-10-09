package com.adasoft.phase.rest.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;


public abstract class AbstractLabelService {
	
	private final String FIXED_URL = "http://192.168.15.10";
	
	protected String buildUrl(Request request) {
        return FIXED_URL + request.getUrl();
    }

    protected HttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected HttpResponse executeRequest(HttpUriRequest request) throws IOException {
        return createHttpClient().execute(request);
    }

    protected StringBuilder readResponse(HttpEntity responseEntity) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8))) {
            StringBuilder responseText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseText.append(line);
            }
            return responseText;
        }
    }
}
