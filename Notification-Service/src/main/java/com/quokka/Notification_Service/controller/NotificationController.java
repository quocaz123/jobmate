package com.quokka.Notification_Service.controller;

import com.quokka.Notification_Service.dto.ApiResponse;
import com.quokka.Notification_Service.dto.request.NotificationRequest;
import com.quokka.Notification_Service.dto.response.NotificationResponse;
import com.quokka.Notification_Service.service.NotificationService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @PostMapping()
    public ApiResponse<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendNotification(request));
    }

    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.success(notificationService.getNotificationsByUserId());
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }
}
