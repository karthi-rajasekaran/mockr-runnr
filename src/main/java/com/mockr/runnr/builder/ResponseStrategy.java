package com.mockr.runnr.builder;

import org.springframework.http.ResponseEntity;

public interface ResponseStrategy {

    ResponseEntity<?> handle(ResponseBuilder builder);

    String getContentType();

    String getCharacterEncoding();
}
