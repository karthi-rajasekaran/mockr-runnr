package com.mockr.runnr.matcher.body;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses JSON bodies and flattens nested objects and arrays.
 * 
 * Examples:
 * {"user": {"name": "John"}} → {user.name: "John"}
 * {"items": [{"id": 1}]} → {items[0].id: 1}
 * {"matrix": [[1, 2]]} → {matrix[0][0]: 1}
 */
public class JsonBodyParser implements BodyParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonBodyParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_FLATTEN_DEPTH = 10;

    @Override
    public Map<String, Object> parse(String body) throws BodyParseException {
        if (body == null || body.isBlank()) {
            return new HashMap<>();
        }

        try {
            Map<String, Object> parsed = mapper.readValue(body, Map.class);
            return flatten(parsed, "", 0);
        } catch (Exception e) {
            throw new BodyParseException("Failed to parse JSON body", e);
        }
    }

    /**
     * Recursively flatten nested structures.
     * 
     * Maps → dot notation: prefix.key
     * Lists → bracket notation: prefix[index]
     * Primitives → terminal values
     */
    private Map<String, Object> flatten(Object obj, String prefix, int depth) {
        Map<String, Object> result = new HashMap<>();

        if (depth > MAX_FLATTEN_DEPTH) {
            logger.warn("Max flattening depth {} exceeded at prefix: {}", MAX_FLATTEN_DEPTH, prefix);
            return result;
        }

        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String newKey = prefix.isEmpty()
                        ? entry.getKey()
                        : prefix + "." + entry.getKey();
                result.putAll(flatten(entry.getValue(), newKey, depth + 1));
            }
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                String newKey = prefix + "[" + i + "]";
                result.putAll(flatten(list.get(i), newKey, depth + 1));
            }
        } else {
            // Primitive: String, Number, Boolean, null
            result.put(prefix, obj);
        }

        return result;
    }
}
