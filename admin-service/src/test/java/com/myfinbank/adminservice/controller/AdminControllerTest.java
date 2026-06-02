package com.myfinbank.adminservice.controller;

import com.myfinbank.adminservice.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AdminControllerTest {

    @Test
    void logoutReturnsSuccessMessage() {
        AdminController controller = new AdminController(mock(AdminService.class));

        ResponseEntity<?> response = controller.logout();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Admin logout successful", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
