package com.quokka.Notification_Service.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    BAD_REQUEST(1400, "Bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1401, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1403, "Access denied", HttpStatus.FORBIDDEN),
    NOT_FOUND(1404, "Resource not found", HttpStatus.NOT_FOUND),
    CONFLICT(1409, "Conflict", HttpStatus.CONFLICT),
    INTERNAL_ERROR(1500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1501, "Unauthenticated", HttpStatus.UNAUTHORIZED),

    // ========== 4xxx: MAIL / EMAIL ==========
    MAIL_SEND_FAILED(4001, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    MAIL_TEMPLATE_NOT_FOUND(4002, "Email template not found", HttpStatus.NOT_FOUND),
    MAIL_INVALID_ADDRESS(4003, "Invalid email address", HttpStatus.BAD_REQUEST),
    MAIL_SERVER_UNAVAILABLE(4004, "Mail server unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    MAIL_AUTH_FAILED(4005, "Mail authentication failed", HttpStatus.UNAUTHORIZED),
    MAIL_RATE_LIMITED(4006, "Too many email requests", HttpStatus.TOO_MANY_REQUESTS),

    // ========== 5xxx: NOTIFICATION ==========
    NOTIFICATION_NOT_FOUND(5001, "Notification not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_CREATE_FAILED(5002, "Failed to create notification", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_SEND_FAILED(5003, "Failed to send notification", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_INVALID_USER(5004, "Invalid target user for notification", HttpStatus.BAD_REQUEST),
    NOTIFICATION_TYPE_INVALID(5005, "Invalid notification type", HttpStatus.BAD_REQUEST),
    NOTIFICATION_WEBSOCKET_ERROR(5006, "WebSocket delivery failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_KAFKA_ERROR(5007, "Kafka publish failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_READ_FAILED(5008, "Failed to mark notification as read", HttpStatus.INTERNAL_SERVER_ERROR);

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
