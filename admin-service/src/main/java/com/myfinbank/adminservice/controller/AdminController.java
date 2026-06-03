package com.myfinbank.adminservice.controller;

import com.myfinbank.adminservice.dto.AdminLoginRequest;
import com.myfinbank.adminservice.dto.AuthResponse;
import com.myfinbank.adminservice.entity.CustomerData;
import com.myfinbank.adminservice.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// REST controller for admin features such as customer activation, loan approval, and customer lookup.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminService.register(request.get("name"), request.get("email"), request.get("password")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Admin logout successful"));
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<CustomerData> getCustomer(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.loadCustomer(id));
    }

    @PostMapping("/customer")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminService.createCustomer(request.get("name"), request.get("email"), request.get("password")));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> searchCustomers(@RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(adminService.searchCustomers(query));
    }

    @GetMapping("/loans")
    public ResponseEntity<?> getAllLoans() {
        return ResponseEntity.ok(adminService.getAllLoans());
    }

    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable(name = "id") Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminService.updateCustomer(id, request.get("name"), request.get("email"), request.get("password")));
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.deleteCustomer(id));
    }

    @PutMapping("/customer/{id}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.deactivateCustomer(id));
    }

    @PutMapping("/customer/{id}/activate")
    public ResponseEntity<String> activate(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.activateCustomer(id));
    }

    @PutMapping("/loan/{id}/approve")
    public ResponseEntity<String> approveLoan(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.approveLoan(id));
    }

    @PutMapping("/loan/{id}/deny")
    public ResponseEntity<String> denyLoan(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.denyLoan(id));
    }

    @PostMapping("/customer/{id}/chat")
    public ResponseEntity<String> chat(@PathVariable(name = "id") Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminService.sendChatMessage(id, request.get("message")));
    }

    @GetMapping("/customer/{id}/chat")
    public ResponseEntity<?> getChatMessages(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminService.getChatMessages(id));
    }
}
