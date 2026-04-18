package com.mockr.runnr.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * MockApiResponse DTO - Response from service layer back to controller.
 * 
 * Contains the mock response details to be sent back to the client.
 * Immutable representation of the mock response.
 */
public class MockApiResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final String contentType;

    private MockApiResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.body = builder.body;
        this.headers = new HashMap<>(builder.headers);
        this.contentType = builder.contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "MockApiResponse{" +
                "statusCode=" + statusCode +
                ", contentType='" + contentType + '\'' +
                ", headers=" + headers.size() + " entries" +
                ", body length=" + (body != null ? body.length() : 0) +
                '}';
    }

    /**
     * Builder for MockApiResponse.
     */
    public static class Builder {
        private int statusCode = 200;
        private String body;
        private final Map<String, String> headers = new HashMap<>();
        private String contentType = "application/json";

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public MockApiResponse build() {
            return new MockApiResponse(this);
        }
    }
}
