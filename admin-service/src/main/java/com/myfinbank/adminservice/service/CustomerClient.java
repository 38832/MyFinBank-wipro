package com.myfinbank.adminservice.service;

import com.myfinbank.adminservice.dto.CustomerDto;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CustomerClient {

    private final RestTemplate restTemplate;

    public CustomerClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CustomerDto getCustomerById(Long id) {
        return restTemplate.getForObject("http://customer-service/api/customers/{id}", CustomerDto.class, id);
    }

    public Object searchCustomers(String query) {
        return restTemplate.getForObject("http://customer-service/api/customers/search?query={query}", Object.class, query == null ? "" : query);
    }

    public Object createCustomer(String name, String email, String password) {
        return restTemplate.postForObject("http://customer-service/api/customers/register",
                Map.of("name", name, "email", email, "password", password), Object.class);
    }

    public CustomerDto updateCustomer(Long id, String name, String email, String password) {
        ResponseEntity<CustomerDto> response = restTemplate.exchange(
                "http://customer-service/api/customers/admin/{id}",
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(Map.of("name", name, "email", email, "password", password == null ? "" : password)),
                CustomerDto.class,
                id);
        return response.getBody();
    }

    public String deleteCustomer(Long id) {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://customer-service/api/customers/admin/{id}",
                HttpMethod.DELETE,
                null,
                String.class,
                id);
        return response.getBody();
    }

    public String activateCustomer(Long id) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{id}/activate",
                HttpMethod.PUT, null, String.class, id).getBody();
    }

    public String deactivateCustomer(Long id) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{id}/deactivate",
                HttpMethod.PUT, null, String.class, id).getBody();
    }

    public String approveLoan(Long customerId) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/loan/{customerId}/approve",
                HttpMethod.PUT, null, String.class, customerId).getBody();
    }

    public String denyLoan(Long customerId) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/loan/{customerId}/deny",
                HttpMethod.PUT, null, String.class, customerId).getBody();
    }
}
