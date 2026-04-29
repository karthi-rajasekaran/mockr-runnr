package com.mockr.runnr.matcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified evaluation context containing all request fields.
 * 
 * Single immutable map with full-path keys for unified field resolution:
 * - "header.content-type" → "application/json"
 * - "query.page" → "1"
 * - "path.userId" → "123"
 * - "body.username" → "karthi"
 * - "body.items[0].name" → "Product A"
 * - "body.soap:Body.Element@version" → "1.0"
 * 
 * Values are Objects to support primitives, nulls, and nested structures
 * from body parsers (JSON objects can contain numbers, booleans, etc).
 */
public class EvaluationContext {

    private final Map<String, Object> fields;

    private EvaluationContext(Map<String, Object> fields) {
        this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
    }

    /**
     * Resolve a full-path field key.
     * 
     * @param fieldPath e.g., "body.username", "header.content-type",
     *                  "body.items[0].name"
     * @return Field value or null if not found
     */
    public Object resolveField(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            return null;
        }

        return fields.get(fieldPath);
    }

    /**
     * @return Immutable unified fields map
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing evaluation context with unified field map.
     */
    public static class Builder {
        private final Map<String, Object> fields = new HashMap<>();

        /**
         * Add all fields from a map.
         * 
         * @param fields Map of fields to add
         * @return Builder for chaining
         */
        public Builder fields(Map<String, Object> fields) {
            if (fields != null) {
                this.fields.putAll(fields);
            }
            return this;
        }

        /**
         * Add a single field.
         * 
         * @param key   Full field path (e.g., "header.content-type", "body.username")
         * @param value Field value
         * @return Builder for chaining
         */
        public Builder addField(String key, Object value) {
            this.fields.put(key, value);
            return this;
        }

        public EvaluationContext build() {
            return new EvaluationContext(fields);
        }
    }
}
