package org.bbqqvv.backendecommerce.controller;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
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
    public ApiResponse<Notification> createNotification(@RequestBody Notification notification) {
        return ApiResponse.success(notificationService.createNotification(notification));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        return ApiResponse.success(notificationService.getNotificationsByUserId(userId));
    }
}
