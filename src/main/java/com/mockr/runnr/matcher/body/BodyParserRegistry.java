package com.mockr.runnr.matcher.body;

import com.mockr.runnr.enums.ContentType;
import java.util.Map;

/**
 * Selects appropriate BodyParser based on Content-Type header or Endpoint
 * configuration.
 */
public class BodyParserRegistry {

    private static final Map<String, BodyParser> PARSERS = Map.of(
            "application/json", new JsonBodyParser(),
            "application/x-www-form-urlencoded", new FormBodyParser(),
            "application/xml", new XmlBodyParser(),
            "application/soap+xml", new XmlBodyParser(),
            "text/xml", new XmlBodyParser());

    private static final BodyParser DEFAULT_PARSER = new RawBodyParser();

    /**
     * Select parser for given content type.
     * 
     * @param contentType Content-Type header value (e.g., "application/json;
     *                    charset=utf-8")
     * @return Appropriate parser, or RawBodyParser if type not recognized
     */
    public static BodyParser selectParser(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_PARSER;
        }

        // Extract media type from header (ignore parameters like charset)
        String mediaType = contentType.split(";")[0].toLowerCase().trim();

        return PARSERS.getOrDefault(mediaType, DEFAULT_PARSER);
    }

    /**
     * Select parser for given ContentType enum.
     * 
     * @param contentType ContentType enum value
     * @return Appropriate parser, or RawBodyParser if type not recognized
     */
    public static BodyParser selectParser(ContentType contentType) {
        if (contentType == null) {
            return DEFAULT_PARSER;
        }
        return selectParser(contentType.getMediaType());
    }

    /**
     * Select parser for endpoint request with fallback strategy.
     * 
     * Priority:
     * 1. Endpoint's configured requestContentType (if not null)
     * 2. HTTP Content-Type header (fallback)
     * 3. RawBodyParser (default if both unavailable)
     * 
     * This allows endpoint designers to explicitly control how request bodies
     * are parsed, independent of what the client sends.
     * 
     * @param endpointRequestContentType Endpoint's configured content type (may be
     *                                   null)
     * @param requestContentTypeHeader   HTTP Content-Type header value (may be
     *                                   null)
     * @return Appropriate parser based on priority
     */
    public static BodyParser selectParserForEndpoint(
            ContentType endpointRequestContentType,
            String requestContentTypeHeader) {

        // Primary: Use endpoint's configured content type
        if (endpointRequestContentType != null) {
            return selectParser(endpointRequestContentType);
        }

        // Fallback: Use request header
        return selectParser(requestContentTypeHeader);
    }
}
