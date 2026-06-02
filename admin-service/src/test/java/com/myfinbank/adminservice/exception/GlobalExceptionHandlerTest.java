package com.myfinbank.adminservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void badRequestHandlerReturnsExceptionMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("No pending loan found"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("No pending loan found", response.getBody().get("message"));
    }
}
