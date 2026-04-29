package com.mockr.runnr.web;

import com.mockr.runnr.exception.ApplicationException;
import com.mockr.runnr.exception.DatabaseException;
import com.mockr.runnr.exception.ValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller that throws exceptions for testing the global exception
 * handler.
 */
@RestController
class TestExceptionController {

    @GetMapping("/test/database-error")
    void throwDatabaseError() {
        throw new DatabaseException("Database connection failed");
    }

    @GetMapping("/test/validation-error")
    void throwValidationError() {
        throw new ValidationException("Input validation failed");
    }

    @GetMapping("/test/application-error")
    void throwApplicationError() {
        throw new ApplicationException("Application error occurred");
    }

    @GetMapping("/test/generic-error")
    void throwGenericError() {
        throw new RuntimeException("Unexpected runtime error");
    }
}
