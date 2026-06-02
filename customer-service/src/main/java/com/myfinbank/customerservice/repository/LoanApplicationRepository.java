package com.myfinbank.customerservice.repository;

import com.myfinbank.customerservice.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByCustomerId(Long customerId);
    Optional<LoanApplication> findFirstByCustomerIdAndStatus(Long customerId, String status);
}
