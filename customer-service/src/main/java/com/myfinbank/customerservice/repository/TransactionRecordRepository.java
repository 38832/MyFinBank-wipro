package com.myfinbank.customerservice.repository;

import com.myfinbank.customerservice.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByCustomerId(Long customerId);
}
