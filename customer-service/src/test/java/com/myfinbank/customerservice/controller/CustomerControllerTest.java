package com.myfinbank.customerservice.controller;

import com.myfinbank.customerservice.dto.BalanceResponse;
import com.myfinbank.customerservice.entity.Customer;
import com.myfinbank.customerservice.security.CustomerUserDetails;
import com.myfinbank.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerControllerTest {

    @Test
    void logoutReturnsSuccessMessage() {
        CustomerController controller = new CustomerController(mock(CustomerService.class), "test-internal-token");

        ResponseEntity<?> response = controller.logout();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logout successful", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void getBalanceUsesLoggedInCustomer() {
        CustomerService customerService = mock(CustomerService.class);
        CustomerController controller = new CustomerController(customerService, "test-internal-token");
        Customer customer = new Customer("Asha", "asha@example.com", "secret");
        customer.setId(7L);
        CustomerUserDetails principal = new CustomerUserDetails(customer);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        BalanceResponse balance = new BalanceResponse(7L, 3L, "savings", 2500.0);
        when(customerService.getBalance(7L)).thenReturn(balance);

        ResponseEntity<?> response = controller.getBalance(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(balance, response.getBody());
        verify(customerService).getBalance(7L);
    }
}
