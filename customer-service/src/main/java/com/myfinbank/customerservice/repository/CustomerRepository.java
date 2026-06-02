package com.myfinbank.customerservice.repository;

import com.myfinbank.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
