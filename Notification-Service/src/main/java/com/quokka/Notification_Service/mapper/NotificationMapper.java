package com.quokka.Notification_Service.mapper;

import com.quokka.Notification_Service.dto.request.NotificationRequest;
import com.quokka.Notification_Service.dto.response.NotificationResponse;
import com.quokka.Notification_Service.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toNotification(NotificationRequest request);

    NotificationResponse toNotificationResponse(Notification notification);
}
