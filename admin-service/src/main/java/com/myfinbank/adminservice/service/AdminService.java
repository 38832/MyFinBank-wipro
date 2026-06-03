package com.myfinbank.adminservice.service;

import com.myfinbank.adminservice.dto.AuthResponse;
import com.myfinbank.adminservice.dto.AdminLoginRequest;
import com.myfinbank.adminservice.dto.CustomerDto;
import com.myfinbank.adminservice.entity.*;
import com.myfinbank.adminservice.repository.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Contains admin service logic and connects to customer service for remote lookup and local admin-side state.
@Service
public class AdminService {

    private final CustomerDataRepository customerRepository;
    private final ChatMessageRepository chatRepository;
    private final CustomerClient customerClient;
    private final AdminUserRepository adminUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final com.myfinbank.adminservice.security.JwtUtil jwtUtil;

    public AdminService(CustomerDataRepository customerRepository,
                        ChatMessageRepository chatRepository,
                        CustomerClient customerClient,
                        AdminUserRepository adminUserRepository,
                        AuthenticationManager authenticationManager,
                        PasswordEncoder passwordEncoder,
                        com.myfinbank.adminservice.security.JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.chatRepository = chatRepository;
        this.customerClient = customerClient;
        this.adminUserRepository = adminUserRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(String name, String email, String password) {
        if (adminUserRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Admin already exists with this email");
        }
        AdminUser adminUser = new AdminUser(email, name, passwordEncoder.encode(password));
        adminUserRepository.save(adminUser);
        return new AuthResponse(null, "Admin registered successfully");
    }

    public AuthResponse login(AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
        AdminUser adminUser = adminUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        return new AuthResponse(token, "Admin login successful", adminUser.getName(), adminUser.getEmail());
    }

    public Object createCustomer(String name, String email, String password) {
        return customerClient.createCustomer(name, email, password);
    }

    public Object searchCustomers(String query) {
        return customerClient.searchCustomers(query);
    }

    public Object getAllLoans() {
        return customerClient.getAllLoans();
    }

    @Transactional
    public CustomerData loadCustomer(Long customerId) {
        CustomerDto remoteCustomer = customerClient.getCustomerById(customerId);
        if (remoteCustomer == null) {
            throw new IllegalArgumentException("Customer not found in customer service");
        }
        CustomerData local = customerRepository.findByEmail(remoteCustomer.getEmail())
                .orElseGet(() -> new CustomerData(remoteCustomer.getName(), remoteCustomer.getEmail()));
        local.setActive(remoteCustomer.isActive());
        local.setName(remoteCustomer.getName());
        local.setEmail(remoteCustomer.getEmail());
        return customerRepository.save(local);
    }

    @Transactional
    public CustomerData updateCustomer(Long customerId, String name, String email, String password) {
        CustomerDto remoteCustomer = customerClient.updateCustomer(customerId, name, email, password);
        CustomerData local = customerRepository.findByEmail(remoteCustomer.getEmail())
                .orElseGet(() -> new CustomerData(remoteCustomer.getName(), remoteCustomer.getEmail()));
        local.setActive(remoteCustomer.isActive());
        local.setName(remoteCustomer.getName());
        local.setEmail(remoteCustomer.getEmail());
        return customerRepository.save(local);
    }

    @Transactional
    public String deleteCustomer(Long customerId) {
        customerRepository.findById(customerId).ifPresent(customerRepository::delete);
        return customerClient.deleteCustomer(customerId);
    }

    @Transactional
    public String deactivateCustomer(Long customerId) {
        customerClient.deactivateCustomer(customerId);
        CustomerData data = loadCustomer(customerId);
        data.setActive(false);
        customerRepository.save(data);
        return "Customer deactivated";
    }

    @Transactional
    public String activateCustomer(Long customerId) {
        customerClient.activateCustomer(customerId);
        CustomerData data = loadCustomer(customerId);
        data.setActive(true);
        customerRepository.save(data);
        return "Customer activated";
    }

    @Transactional
    public String approveLoan(Long customerId) {
        return customerClient.approveLoan(customerId);
    }

    @Transactional
    public String denyLoan(Long customerId) {
        return customerClient.denyLoan(customerId);
    }

    @Transactional
    public String sendChatMessage(Long customerId, String message) {
        return customerClient.sendChatMessage(customerId, message);
    }

    public Object getChatMessages(Long customerId) {
        return customerClient.getChatMessages(customerId);
    }
}
