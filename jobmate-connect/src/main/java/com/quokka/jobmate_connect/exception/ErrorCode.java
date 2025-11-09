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
    INVALID_OTP(1502, "Invalid OTP", HttpStatus.BAD_REQUEST),
    RESEND_OTP_LIMIT(1503, "Resend OTP limit exceeded. Try again later.", HttpStatus.TOO_MANY_REQUESTS),

    // ========== 2xxx: USER ==========
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "User already exists", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH(2003, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    PASSWORD_ALREADY_SET(2004, "Password has already been set for this user", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(2005, "Password must be at least 8 characters long", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_LONG(2006, "Password must not exceed 50 characters", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(2007, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(2008, "Email already exists", HttpStatus.CONFLICT),
    LOCATION_ALREADY_SET(2009, "Location has already been set and cannot be changed", HttpStatus.BAD_REQUEST),

    // ========== 3xxx: JOB ==========
    JOB_CANNOT_BE_UPDATED(3000, "Job cannot be updated in its current status", HttpStatus.BAD_REQUEST),

    // ========== 4xxx: APPLICATION ==========
    JOB_NOT_FOUND(4000, "Job not found", HttpStatus.NOT_FOUND),
    JOB_NOT_AVAILABLE(4001, "Job is not available for application", HttpStatus.BAD_REQUEST),
    ALREADY_APPLIED(4002, "You have already applied for this job", HttpStatus.CONFLICT),
    APPLICATION_NOT_FOUND(4003, "Application not found", HttpStatus.NOT_FOUND),
    CANNOT_CANCEL_APPLICATION(4004, "Cannot cancel application at this stage", HttpStatus.BAD_REQUEST),
    ALREADY_RATED(4005, "You have already rated this application", HttpStatus.CONFLICT),
    CANNOT_RATE_YOURSELF(4006, "Cannot rate yourself", HttpStatus.BAD_REQUEST),
    INVALID_RATING_SCORE(4007, "Rating score must be between 1 and 5", HttpStatus.BAD_REQUEST),

    // ========== 5xxx: FILE UPLOAD ==========
    FILE_UPLOAD_FAILED(5001, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ========== 6xxx: RATING ==========
    ALREADY_EXISTS(6001, "User has already rated this user for the specified job.", HttpStatus.CONFLICT),
    CANNOT_RATE_SELF(6002, "Users cannot rate themselves.", HttpStatus.BAD_REQUEST),
    RATING_NOT_FOUND(6003, "Rating not found", HttpStatus.NOT_FOUND),
    RATING_NOT_ALLOWED(6004, "Rating is not allowed for this user or job", HttpStatus.BAD_REQUEST),
    RATING_NOT_ALLOWED_BEFORE_DEADLINE(6005, "Rating is not allowed before the job completion deadline", HttpStatus.BAD_REQUEST)
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
