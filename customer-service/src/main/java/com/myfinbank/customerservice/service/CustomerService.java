package com.myfinbank.customerservice.service;

import com.myfinbank.customerservice.dto.*;
import com.myfinbank.customerservice.entity.*;
import com.myfinbank.customerservice.repository.*;
import com.myfinbank.customerservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Business logic for customer operations including account balance changes, transfers, loan applications, and notifications.
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRepository;
    private final LoanApplicationRepository loanRepository;
    private final ChatMessageRepository chatRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final NotificationClient notificationClient;

    public CustomerService(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           TransactionRecordRepository transactionRepository,
                           LoanApplicationRepository loanRepository,
                           ChatMessageRepository chatRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           NotificationClient notificationClient) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.chatRepository = chatRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.notificationClient = notificationClient;
    }

    public AuthResponse register(RegistrationRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse(null, "Email already registered");
        }
        Customer customer = new Customer(request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()));
        Customer saved = customerRepository.save(customer);
        Account account = new Account(saved);
        accountRepository.save(account);
        return new AuthResponse(null, "Customer registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return new AuthResponse(token, "Login successful", customer.getId(), customer.getName(), customer.getEmail());
    }

    @Transactional
    public String deposit(AmountRequest request) {
        Customer customer = findActiveCustomer(request.getCustomerId());
        Account account = findAccount(customer);
        account.setBalance(account.getBalance() + request.getAmount());
        accountRepository.save(account);
        saveTransaction(customer.getId(), "DEPOSIT", request.getAmount(), "Deposit to account");
        return "Deposit successful. New balance: " + account.getBalance();
    }

    @Transactional
    public String withdraw(AmountRequest request) {
        Customer customer = findActiveCustomer(request.getCustomerId());
        Account account = findAccount(customer);
        if (account.getBalance() < request.getAmount()) {
            return "Insufficient balance";
        }
        account.setBalance(account.getBalance() - request.getAmount());
        accountRepository.save(account);
        saveTransaction(customer.getId(), "WITHDRAW", request.getAmount(), "Withdraw from account");
        if (account.getBalance() == 0) {
            notificationClient.sendLowBalanceAlert(customer.getId(), account.getBalance());
        }
        return "Withdraw successful. New balance: " + account.getBalance();
    }

    @Transactional
    public String transfer(TransferRequest request) {
        Customer source = findActiveCustomer(request.getSourceCustomerId());
        Customer target = customerRepository.findByEmail(request.getTargetEmail())
                .orElseThrow(() -> new IllegalArgumentException("Target customer not found"));
        Account sourceAccount = findAccount(source);
        Account targetAccount = findAccount(target);
        if (sourceAccount.getBalance() < request.getAmount()) {
            return "Insufficient balance for transfer";
        }
        sourceAccount.setBalance(sourceAccount.getBalance() - request.getAmount());
        targetAccount.setBalance(targetAccount.getBalance() + request.getAmount());
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        saveTransaction(source.getId(), "TRANSFER_OUT", request.getAmount(), "Transfer to " + target.getEmail());
        saveTransaction(target.getId(), "TRANSFER_IN", request.getAmount(), "Transfer from " + source.getEmail());
        return "Transfer completed successfully";
    }

    @Transactional
    public String applyLoan(LoanRequest request) {
        findActiveCustomer(request.getCustomerId());
        LoanApplication loan = new LoanApplication(
                request.getCustomerId(),
                request.getAmount(),
                request.getLoanType(),
                request.getTermMonths(),
                request.getRate());
        loanRepository.save(loan);
        return "Loan application submitted";
    }

    public double calculateEmi(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 1200.0;
        return (principal * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -months));
    }

    public String chat(ChatRequest request) {
        Customer customer = findActiveCustomer(request.getCustomerId());
        ChatMessage chatMessage = new ChatMessage(customer.getId(), request.getSender(), request.getMessage());
        chatRepository.save(chatMessage);
        return "Message sent to bank support";
    }

    public List<ChatMessage> getChatMessages(Long customerId) {
        findCustomerById(customerId);
        return chatRepository.findByCustomerId(customerId);
    }

    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    @Transactional
    public Customer updateCustomer(Long id, RegistrationRequest request) {
        Customer customer = findCustomerById(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public String deleteCustomer(Long id) {
        Customer customer = findCustomerById(id);
        accountRepository.deleteByCustomer(customer);
        customerRepository.delete(customer);
        return "Customer deleted";
    }

    @Transactional
    public String setCustomerActive(Long id, boolean active) {
        Customer customer = findCustomerById(id);
        customer.setActive(active);
        customerRepository.save(customer);
        return active ? "Customer activated" : "Customer deactivated";
    }

    public List<TransactionRecord> getTransactions(Long customerId) {
        findCustomerById(customerId);
        return transactionRepository.findByCustomerId(customerId);
    }

    public List<LoanApplication> getLoans(Long customerId) {
        findCustomerById(customerId);
        return loanRepository.findByCustomerId(customerId);
    }

    public BalanceResponse getBalance(Long customerId) {
        Customer customer = findActiveCustomer(customerId);
        Account account = findAccount(customer);
        return new BalanceResponse(customer.getId(), account.getId(), account.getAccountType(), account.getBalance());
    }

    public List<LoanApplication> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional
    public String decidePendingLoan(Long customerId, String status) {
        LoanApplication loan = loanRepository.findFirstByCustomerIdAndStatus(customerId, "PENDING")
                .orElseThrow(() -> new IllegalArgumentException("No pending loan found"));
        loan.setStatus(status);
        loanRepository.save(loan);
        return "Loan " + status.toLowerCase();
    }

    private void saveTransaction(Long customerId, String type, double amount, String description) {
        TransactionRecord record = new TransactionRecord(UUID.randomUUID().toString(), customerId, type, amount, description);
        transactionRepository.save(record);
    }

    private Customer findActiveCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found or inactive"));
    }

    private Account findAccount(Customer customer) {
        return accountRepository.findByCustomer(customer)
                .orElseThrow(() -> new IllegalStateException("Account not found for customer"));
    }
}
