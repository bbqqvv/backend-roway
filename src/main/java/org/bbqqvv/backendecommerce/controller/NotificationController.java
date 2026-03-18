package org.bbqqvv.backendecommerce.controller;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bbqqvv.backendecommerce.entity.Notification;
import org.bbqqvv.backendecommerce.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
	
    private NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }
}
