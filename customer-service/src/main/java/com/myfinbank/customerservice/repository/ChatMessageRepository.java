package com.myfinbank.customerservice.repository;

import com.myfinbank.customerservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCustomerId(Long customerId);
}
