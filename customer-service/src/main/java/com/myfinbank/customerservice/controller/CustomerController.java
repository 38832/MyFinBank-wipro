package com.myfinbank.customerservice.controller;

import com.myfinbank.customerservice.dto.*;
import com.myfinbank.customerservice.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controller exposing REST endpoints for customer registration, authentication, banking transactions, loans, and chat.
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(customerService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(customerService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody AmountRequest request) {
        return ResponseEntity.ok(customerService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody AmountRequest request) {
        return ResponseEntity.ok(customerService.withdraw(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(customerService.transfer(request));
    }

    @PostMapping("/apply-loan")
    public ResponseEntity<?> applyLoan(@RequestBody LoanRequest request) {
        return ResponseEntity.ok(customerService.applyLoan(request));
    }

    @GetMapping("/calculate-emi")
    public ResponseEntity<?> calculateEmi(@RequestParam double principal,
                                          @RequestParam double rate,
                                          @RequestParam int months) {
        return ResponseEntity.ok(customerService.calculateEmi(principal, rate, months));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(customerService.chat(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getTransactions(id));
    }

    @GetMapping("/{id}/loans")
    public ResponseEntity<?> getLoans(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getLoans(id));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    @PutMapping("/admin/{id}/activate")
    public ResponseEntity<?> activateCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.setCustomerActive(id, true));
    }

    @PutMapping("/admin/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.setCustomerActive(id, false));
    }

    @PutMapping("/admin/loan/{customerId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.decidePendingLoan(customerId, "APPROVED"));
    }

    @PutMapping("/admin/loan/{customerId}/deny")
    public ResponseEntity<?> denyLoan(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.decidePendingLoan(customerId, "DENIED"));
    }
}
