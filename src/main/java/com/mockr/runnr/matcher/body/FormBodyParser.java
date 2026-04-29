package com.mockr.runnr.matcher.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses form-encoded bodies (application/x-www-form-urlencoded).
 * 
 * Used for OAuth2 token requests and simple key-value form data.
 * 
 * Example:
 * grant_type=password&username=admin&password=Pass123
 * → {grant_type: "password", username: "admin", password: "Pass123"}
 */
public class FormBodyParser implements BodyParser {

    private static final Logger logger = LoggerFactory.getLogger(FormBodyParser.class);

    @Override
    public Map<String, Object> parse(String body) throws BodyParseException {
        Map<String, Object> result = new HashMap<>();

        if (body == null || body.isBlank()) {
            return result;
        }

        try {
            for (String pair : body.split("&")) {
                if (pair.isBlank()) {
                    continue;
                }

                String[] parts = pair.split("=", 2);
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8).toLowerCase();
                String value = parts.length > 1
                        ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                        : "";

                result.put(key, value);
            }

            return result;
        } catch (Exception e) {
            throw new BodyParseException("Failed to parse form-encoded body", e);
        }
    }
}
