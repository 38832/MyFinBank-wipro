package com.myfinbank.adminservice.repository;

import com.myfinbank.adminservice.entity.LoanApplicationData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoanApplicationDataRepository extends JpaRepository<LoanApplicationData, Long> {
    Optional<LoanApplicationData> findFirstByCustomerIdAndStatus(Long customerId, String status);
}
