package com.myfinbank.adminservice.service;

import com.myfinbank.adminservice.dto.CustomerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String internalToken;

    public CustomerClient(RestTemplate restTemplate,
                          @Value("${internal.service.token:myfinbank-internal-token}") String internalToken) {
        this.restTemplate = restTemplate;
        this.internalToken = internalToken;
    }

    private HttpEntity<?> internalEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Token", internalToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<?> internalEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Token", internalToken);
        return new HttpEntity<>(body, headers);
    }

    public CustomerDto getCustomerById(Long id) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{id}",
                HttpMethod.GET, internalEntity(), CustomerDto.class, id).getBody();
    }

    public Object searchCustomers(String query) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/search?query={query}",
                HttpMethod.GET, internalEntity(), Object.class, query == null ? "" : query).getBody();
    }

    public Object getAllLoans() {
        return restTemplate.exchange("http://customer-service/api/customers/admin/loans",
                HttpMethod.GET, internalEntity(), Object.class).getBody();
    }

    public Object createCustomer(String name, String email, String password) {
        return restTemplate.postForObject("http://customer-service/api/customers/register",
                Map.of("name", name, "email", email, "password", password), Object.class);
    }

    public CustomerDto updateCustomer(Long id, String name, String email, String password) {
        ResponseEntity<CustomerDto> response = restTemplate.exchange(
                "http://customer-service/api/customers/admin/{id}",
                HttpMethod.PUT,
                internalEntity(Map.of("name", name, "email", email, "password", password == null ? "" : password)),
                CustomerDto.class,
                id);
        return response.getBody();
    }

    public String deleteCustomer(Long id) {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://customer-service/api/customers/admin/{id}",
                HttpMethod.DELETE,
                internalEntity(),
                String.class,
                id);
        return response.getBody();
    }

    public String activateCustomer(Long id) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{id}/activate",
                HttpMethod.PUT, internalEntity(), String.class, id).getBody();
    }

    public String deactivateCustomer(Long id) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{id}/deactivate",
                HttpMethod.PUT, internalEntity(), String.class, id).getBody();
    }

    public String approveLoan(Long customerId) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/loan/{customerId}/approve",
                HttpMethod.PUT, internalEntity(), String.class, customerId).getBody();
    }

    public String denyLoan(Long customerId) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/loan/{customerId}/deny",
                HttpMethod.PUT, internalEntity(), String.class, customerId).getBody();
    }

    public Object getChatMessages(Long customerId) {
        return restTemplate.exchange("http://customer-service/api/customers/admin/{customerId}/chat",
                HttpMethod.GET, internalEntity(), Object.class, customerId).getBody();
    }

    public String sendChatMessage(Long customerId, String message) {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://customer-service/api/customers/admin/{customerId}/chat",
                HttpMethod.POST,
                internalEntity(Map.of("sender", "ADMIN", "message", message)),
                String.class,
                customerId);
        return response.getBody();
    }
}
