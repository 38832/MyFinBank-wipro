package com.myfinbank.notificationservice.controller;

import com.myfinbank.notificationservice.entity.NotificationEvent;
import com.myfinbank.notificationservice.repository.NotificationEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationEventRepository repository;

    public NotificationController(NotificationEventRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, Object> payload) {
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        String subject = payload.get("subject").toString();
        String message = payload.get("message").toString();
        NotificationEvent event = new NotificationEvent(customerId, subject, message);
        repository.save(event);
        return ResponseEntity.ok(Map.of("status", "notification queued", "customerId", customerId));
    }
}
