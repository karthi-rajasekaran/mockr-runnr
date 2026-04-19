package com.mockr.runnr.builder.strategies;

import com.mockr.runnr.builder.ResponseBuilder;
import com.mockr.runnr.builder.ResponseBuilderException;
import com.mockr.runnr.builder.ResponseStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileResponseStrategy implements ResponseStrategy {

    @Override
    public ResponseEntity<?> handle(ResponseBuilder builder) {
        String filePath = builder.getFilePath();

        if (filePath == null || filePath.isBlank()) {
            throw new ResponseBuilderException("File path required for FILE response type");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ResponseBuilderException("File not found: " + filePath);
            }

            byte[] fileContent = Files.readAllBytes(path);
            HttpHeaders headers = builder.buildHeaders();

            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return ResponseEntity
                    .status(builder.getStatus())
                    .headers(headers)
                    .body(fileContent);

        } catch (IOException e) {
            throw new ResponseBuilderException("Failed to read file: " + filePath, e);
        }
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }
}
