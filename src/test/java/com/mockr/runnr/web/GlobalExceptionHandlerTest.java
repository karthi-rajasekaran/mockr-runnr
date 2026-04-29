package com.mockr.runnr.web;

import com.mockr.runnr.exception.ApplicationException;
import com.mockr.runnr.exception.DatabaseException;
import com.mockr.runnr.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for GlobalExceptionHandler error handling behavior.
 */
@WebMvcTest({ GlobalExceptionHandler.class, TestExceptionController.class })
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleDatabaseException_Returns503() throws Exception {
        mockMvc.perform(get("/test/database-error")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.statusCode", equalTo(503)))
                .andExpect(jsonPath("$.error", equalTo("DATABASE_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }

    @Test
    void handleValidationException_Returns400() throws Exception {
        mockMvc.perform(get("/test/validation-error")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", equalTo(400)))
                .andExpect(jsonPath("$.error", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }

    @Test
    void handleApplicationException_Returns400() throws Exception {
        mockMvc.perform(get("/test/application-error")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", equalTo(400)))
                .andExpect(jsonPath("$.error", equalTo("APPLICATION_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }

    @Test
    void handleGenericException_Returns500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode", equalTo(500)))
                .andExpect(jsonPath("$.error", equalTo("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }
}
