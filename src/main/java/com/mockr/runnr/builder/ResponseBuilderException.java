package com.mockr.runnr.builder;

public class ResponseBuilderException extends RuntimeException {

    public ResponseBuilderException(String message) {
        super(message);
    }

    public ResponseBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
