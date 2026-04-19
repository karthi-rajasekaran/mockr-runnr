package com.mockr.runnr.builder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class ResponseBuilder {

    private int status = 200;
    private String body;
    private String filePath;
    private ResponseType responseType = ResponseType.JSON;
    private final Map<String, String> customHeaders = new HashMap<>();

    public ResponseBuilder withStatus(int statusCode) {
        if (statusCode < 100 || statusCode > 599) {
            throw new ResponseBuilderException("Invalid HTTP status code: " + statusCode);
        }
        this.status = statusCode;
        return this;
    }

    public ResponseBuilder withHeader(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new ResponseBuilderException("Header key cannot be null or empty");
        }
        customHeaders.put(key, value);
        return this;
    }

    public ResponseBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public ResponseBuilder withResponseType(ResponseType responseType) {
        if (responseType == null) {
            throw new ResponseBuilderException("Response type cannot be null");
        }
        this.responseType = responseType;
        return this;
    }

    public ResponseBuilder withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public ResponseEntity<?> build(ResponseStrategyFactory strategyFactory) {
        try {
            ResponseStrategy strategy = strategyFactory.getStrategy(responseType);
            ResponseEntity<?> response = strategy.handle(this);
            log.debug("Response built: status={}, type={}, body_size={}",
                    status, responseType, body != null ? body.length() : 0);
            return response;
        } catch (ResponseBuilderException e) {
            log.error("Failed to build response: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ResponseEntity<?> buildRaw(byte[] data, ResponseStrategyFactory strategyFactory) {
        try {
            HttpHeaders headers = buildHeaders();
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.set(HttpHeaders.CONTENT_TYPE, ResponseType.BINARY.getContentType());
            }
            ResponseEntity<?> response = ResponseEntity
                    .status(status)
                    .headers(headers)
                    .body(data);
            log.debug("Raw response built: status={}, size={} bytes", status, data.length);
            return response;
        } catch (Exception e) {
            log.error("Failed to build raw response", e);
            throw new ResponseBuilderException("Failed to build raw response", e);
        }
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public String getFilePath() {
        return filePath;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public Map<String, String> getCustomHeaders() {
        return new HashMap<>(customHeaders);
    }

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        customHeaders.forEach(headers::set);
        return headers;
    }

    @Override
    public String toString() {
        return "ResponseBuilder{" +
                "status=" + status +
                ", responseType=" + responseType +
                ", body_size=" + (body != null ? body.length() : 0) +
                ", headers=" + customHeaders.size() +
                '}';
    }
}
