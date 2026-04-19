package com.mockr.runnr.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record MockResponse(
        int statusCode,
        String body,
        String contentType,
        Map<String, String> headers) {

    public MockResponse {
        // Defensive copy
        headers = headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(headers));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int statusCode = 200;
        private String body;
        private String contentType = "application/json";
        private final Map<String, String> headers = new HashMap<>();

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public MockResponse build() {
            return new MockResponse(statusCode, body, contentType, headers);
        }
    }
}
