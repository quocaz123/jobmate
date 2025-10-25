package com.quokka.Chat_Service.exception;

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
    INVALID_OTP(1502, "Invalid OTP", HttpStatus.BAD_REQUEST),
    RESEND_OTP_LIMIT(1503, "Resend OTP limit exceeded. Try again later.", HttpStatus.TOO_MANY_REQUESTS),

    // ========== 2xxx: USER ==========
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "User already exists", HttpStatus.CONFLICT),


    CONVERSATION_NOT_FOUND(3001, "Conversation not found", HttpStatus.NOT_FOUND),
    USER_NOT_IN_CONVERSATION(3002, "User is not a participant in the conversation", HttpStatus.FORBIDDEN)


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
