package com.mockr.runnr.enums;

/**
 * ContentType enum - Represents supported request/response content types.
 * 
 * Used by Endpoint.requestContentType to explicitly define how incoming request
 * bodies
 * should be parsed and flattened for condition evaluation.
 */
public enum ContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    APPLICATION_YAML("application/yaml"),
    APPLICATION_JSON_LD("application/ld+json");

    private final String mediaType;

    ContentType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Get the media type string for this content type.
     * 
     * @return Media type (e.g., "application/json")
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Find ContentType enum by media type string.
     * Handles case-insensitive lookup and extracts media type from headers
     * that include parameters (e.g., "application/json; charset=utf-8").
     * 
     * @param mediaTypeString Media type string or header value
     * @return ContentType enum, or null if not found
     */
    public static ContentType fromMediaType(String mediaTypeString) {
        if (mediaTypeString == null || mediaTypeString.isBlank()) {
            return null;
        }

        // Extract media type from header (ignore parameters like charset)
        String cleanMediaType = mediaTypeString.split(";")[0].toLowerCase().trim();

        for (ContentType type : values()) {
            if (type.mediaType.equalsIgnoreCase(cleanMediaType)) {
                return type;
            }
        }

        return null;
    }
}
