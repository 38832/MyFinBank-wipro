package com.myfinbank.notificationservice.repository;

import com.myfinbank.notificationservice.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
}
