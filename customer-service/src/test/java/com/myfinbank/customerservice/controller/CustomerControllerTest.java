package com.myfinbank.customerservice.controller;

import com.myfinbank.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomerControllerTest {

    @Test
    void logoutReturnsSuccessMessage() {
        CustomerController controller = new CustomerController(mock(CustomerService.class));

        ResponseEntity<?> response = controller.logout();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logout successful", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
