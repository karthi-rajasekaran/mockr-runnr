package com.mockr.runnr.matcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EvaluationContext {

    private final Map<String, String> headers;
    private final Map<String, String> queryParameters;
    private final Map<String, String> pathVariables;

    private EvaluationContext(Builder builder) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.queryParameters = Collections.unmodifiableMap(new HashMap<>(builder.queryParameters));
        this.pathVariables = Collections.unmodifiableMap(new HashMap<>(builder.pathVariables));
    }

    public String resolve(String key) {
        if (key == null || !key.contains(".")) {
            return null;
        }
        int dotIndex = key.indexOf('.');
        String source = key.substring(0, dotIndex).toLowerCase();
        String name = key.substring(dotIndex + 1);

        return switch (source) {
            case "header" -> resolveFromMap(headers, name, true);
            case "query" -> resolveFromMap(queryParameters, name, false);
            case "path" -> resolveFromMap(pathVariables, name, false);
            default -> null;
        };
    }

    private String resolveFromMap(Map<String, String> map, String key, boolean caseInsensitive) {
        if (caseInsensitive) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
            return null;
        }
        return map.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> queryParameters = new HashMap<>();
        private final Map<String, String> pathVariables = new HashMap<>();

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters.putAll(queryParameters);
            return this;
        }

        public Builder pathVariables(Map<String, String> pathVariables) {
            this.pathVariables.putAll(pathVariables);
            return this;
        }

        public EvaluationContext build() {
            return new EvaluationContext(this);
        }
    }
}
