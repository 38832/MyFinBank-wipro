package com.myfinbank.notificationservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void badRequestHandlerReturnsExceptionMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("Invalid notification"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid notification", response.getBody().get("message"));
    }
}
