package com.quokka.jobmate_connect.exception;


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

    // ========== 3xxx: USER ==========
    USER_NOT_FOUND(3001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(3002, "User already exists", HttpStatus.CONFLICT),

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
