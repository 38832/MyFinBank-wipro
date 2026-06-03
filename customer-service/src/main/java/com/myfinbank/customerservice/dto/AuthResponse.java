package com.myfinbank.customerservice.dto;

public class AuthResponse {
    private String token;
    private String message;
    private Long customerId;
    private String name;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public AuthResponse(String token, String message, Long customerId) {
        this.token = token;
        this.message = message;
        this.customerId = customerId;
    }

    public AuthResponse(String token, String message, Long customerId, String name, String email) {
        this.token = token;
        this.message = message;
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
