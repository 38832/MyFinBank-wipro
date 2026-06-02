package com.myfinbank.customerservice.repository;

import com.myfinbank.customerservice.entity.Account;
import com.myfinbank.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCustomer(Customer customer);
    void deleteByCustomer(Customer customer);
}
