package com.mockr.runnr.builder;

public enum ResponseType {
    JSON("application/json", "UTF-8"),
    XML("application/xml", "UTF-8"),
    HTML("text/html", "UTF-8"),
    FILE("application/octet-stream", null),
    SOAP("application/soap+xml", "UTF-8"),
    GRAPHQL("application/graphql+json", "UTF-8"),
    BINARY("application/octet-stream", null);

    private final String contentType;
    private final String encoding;

    ResponseType(String contentType, String encoding) {
        this.contentType = contentType;
        this.encoding = encoding;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEncoding() {
        return encoding;
    }
}
