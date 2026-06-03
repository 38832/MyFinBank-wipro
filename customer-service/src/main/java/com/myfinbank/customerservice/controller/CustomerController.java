package com.myfinbank.customerservice.controller;

import com.myfinbank.customerservice.dto.*;
import com.myfinbank.customerservice.security.CustomerUserDetails;
import com.myfinbank.customerservice.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controller exposing REST endpoints for customer registration, authentication, banking transactions, loans, and chat.
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final String internalServiceToken;

    public CustomerController(CustomerService customerService,
                              @Value("${internal.service.token:myfinbank-internal-token}") String internalServiceToken) {
        this.customerService = customerService;
        this.internalServiceToken = internalServiceToken;
    }

    private void requireInternalToken(String token) {
        if (!internalServiceToken.equals(token)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Internal service token required");
        }
    }

    private Long loggedInCustomerId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomerUserDetails customerUserDetails)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Customer login required");
        }
        return customerUserDetails.getCustomerId();
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
    public ResponseEntity<?> deposit(@RequestBody AmountRequest request, Authentication authentication) {
        request.setCustomerId(loggedInCustomerId(authentication));
        return ResponseEntity.ok(customerService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody AmountRequest request, Authentication authentication) {
        request.setCustomerId(loggedInCustomerId(authentication));
        return ResponseEntity.ok(customerService.withdraw(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        request.setSourceCustomerId(loggedInCustomerId(authentication));
        return ResponseEntity.ok(customerService.transfer(request));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        return ResponseEntity.ok(customerService.getBalance(loggedInCustomerId(authentication)));
    }

    @PostMapping("/apply-loan")
    public ResponseEntity<?> applyLoan(@RequestBody LoanRequest request, Authentication authentication) {
        request.setCustomerId(loggedInCustomerId(authentication));
        return ResponseEntity.ok(customerService.applyLoan(request));
    }

    @GetMapping("/calculate-emi")
    public ResponseEntity<?> calculateEmi(@RequestParam(name = "principal") double principal,
                                          @RequestParam(name = "rate") double rate,
                                          @RequestParam(name = "months") int months) {
        return ResponseEntity.ok(customerService.calculateEmi(principal, rate, months));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Authentication authentication) {
        request.setCustomerId(loggedInCustomerId(authentication));
        request.setSender("CUSTOMER");
        return ResponseEntity.ok(customerService.chat(request));
    }

    @GetMapping("/chat")
    public ResponseEntity<?> getChatMessages(Authentication authentication) {
        return ResponseEntity.ok(customerService.getChatMessages(loggedInCustomerId(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable(name = "id") Long id, Authentication authentication) {
        return ResponseEntity.ok(customerService.getTransactions(loggedInCustomerId(authentication)));
    }

    @GetMapping("/{id}/loans")
    public ResponseEntity<?> getLoans(@PathVariable(name = "id") Long id, Authentication authentication) {
        return ResponseEntity.ok(customerService.getLoans(loggedInCustomerId(authentication)));
    }

    @GetMapping("/admin/loans")
    public ResponseEntity<?> getAllLoansForAdmin(@RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.getAllLoans());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getCustomerForAdmin(@PathVariable(name = "id") Long id,
                                                 @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @GetMapping("/admin/search")
    public ResponseEntity<?> searchCustomersForAdmin(@RequestParam(name = "query", required = false) String query,
                                                     @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    @GetMapping("/admin/{id}/chat")
    public ResponseEntity<?> getChatMessagesForAdmin(@PathVariable(name = "id") Long id,
                                                     @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.getChatMessages(id));
    }

    @PostMapping("/admin/{id}/chat")
    public ResponseEntity<?> sendChatForAdmin(@PathVariable(name = "id") Long id,
                                              @RequestBody ChatRequest request,
                                              @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        request.setCustomerId(id);
        request.setSender("ADMIN");
        return ResponseEntity.ok(customerService.chat(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable(name = "id") Long id, @RequestBody RegistrationRequest request,
                                            @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteCustomerForAdmin(@PathVariable(name = "id") Long id,
                                                    @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    @PutMapping("/admin/{id}/activate")
    public ResponseEntity<?> activateCustomer(@PathVariable(name = "id") Long id,
                                              @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.setCustomerActive(id, true));
    }

    @PutMapping("/admin/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(@PathVariable(name = "id") Long id,
                                                @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.setCustomerActive(id, false));
    }

    @PutMapping("/admin/loan/{customerId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable(name = "customerId") Long customerId,
                                         @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.decidePendingLoan(customerId, "APPROVED"));
    }

    @PutMapping("/admin/loan/{customerId}/deny")
    public ResponseEntity<?> denyLoan(@PathVariable(name = "customerId") Long customerId,
                                      @RequestHeader(name = "X-Internal-Service-Token", required = false) String token) {
        requireInternalToken(token);
        return ResponseEntity.ok(customerService.decidePendingLoan(customerId, "DENIED"));
    }
}
