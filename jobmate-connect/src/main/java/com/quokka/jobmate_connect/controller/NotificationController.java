package com.quokka.jobmate_connect.controller;


import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.notification.NotificationResponse;
import com.quokka.jobmate_connect.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyNotifications() {
        notificationService.deleteAllMyNotifications();
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }
}
