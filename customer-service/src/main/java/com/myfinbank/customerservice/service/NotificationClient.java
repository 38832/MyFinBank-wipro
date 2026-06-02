package com.myfinbank.customerservice.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationClient {

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendLowBalanceAlert(Long customerId, double balance) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customerId);
        payload.put("subject", "Balance Alert");
        payload.put("message", "Customer " + customerId + " has a balance of " + balance + " which requires attention.");
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("http://notification-service/api/notifications/email", payload, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Notification service returned: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
