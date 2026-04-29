package com.mockr.runnr.matcher.body;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback parser for unknown content types.
 * 
 * Simply stores the entire body as-is under the "raw" key.
 */
public class RawBodyParser implements BodyParser {

    @Override
    public Map<String, Object> parse(String body) throws BodyParseException {
        Map<String, Object> result = new HashMap<>();
        result.put("raw", body);
        return result;
    }
}
