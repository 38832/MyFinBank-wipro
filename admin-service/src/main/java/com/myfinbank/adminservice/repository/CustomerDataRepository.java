package com.myfinbank.adminservice.repository;

import com.myfinbank.adminservice.entity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerDataRepository extends JpaRepository<CustomerData, Long> {
    Optional<CustomerData> findByEmail(String email);
}
