package com.mockr.runnr.builder.strategies;

import com.mockr.runnr.builder.ResponseBuilder;
import com.mockr.runnr.builder.ResponseStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class XmlResponseStrategy implements ResponseStrategy {

    @Override
    public ResponseEntity<?> handle(ResponseBuilder builder) {
        String body = builder.getBody();
        HttpHeaders headers = builder.buildHeaders();

        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_XML);
        }

        return ResponseEntity
                .status(builder.getStatus())
                .headers(headers)
                .body(body);
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }
}
