package com.mockr.runnr.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * MockRequest DTO - Captures all relevant details from incoming HTTP request.
 * 
 * Used to pass request data from controller to service layer for processing.
 * Immutable representation of the incoming request.
 */
public class MockRequest {

    private final String method;
    private final String path;
    private final String body;
    private final Map<String, String> headers;
    private final Map<String, String> queryParameters;
    private final String projectId;

    private MockRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.body = builder.body;
        this.headers = new HashMap<>(builder.headers);
        this.queryParameters = new HashMap<>(builder.queryParameters);
        this.projectId = builder.projectId;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public Map<String, String> getQueryParameters() {
        return new HashMap<>(queryParameters);
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "MockRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", projectId='" + projectId + '\'' +
                ", headers=" + headers.size() + " entries" +
                ", queryParameters=" + queryParameters.size() + " entries" +
                '}';
    }

    /**
     * Builder for MockRequest.
     */
    public static class Builder {
        private String method;
        private String path;
        private String body;
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> queryParameters = new HashMap<>();
        private String projectId;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
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

        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters.putAll(queryParameters);
            return this;
        }

        public Builder queryParameter(String key, String value) {
            this.queryParameters.put(key, value);
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public MockRequest build() {
            return new MockRequest(this);
        }
    }
}
