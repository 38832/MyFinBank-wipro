package com.myfinbank.adminservice.repository;

import com.myfinbank.adminservice.entity.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountDataRepository extends JpaRepository<AccountData, Long> {
    Optional<AccountData> findByCustomerId(Long customerId);
}
