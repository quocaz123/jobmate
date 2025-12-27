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
    ROLE_NOT_FOUND(2010, "Role not found", HttpStatus.NOT_FOUND),
    MISSING_VERIFICATION_FILES(2011, "Please upload both CCCD images and avatar before verifying.",
            HttpStatus.BAD_REQUEST),

    // ========== 3xxx: JOB ==========
    JOB_CANNOT_BE_UPDATED(3000, "Job cannot be updated in its current status", HttpStatus.BAD_REQUEST),
    JOB_INVALID_STATUS_CLOSE(3001, "Job cannot be closed in its current status", HttpStatus.BAD_REQUEST),
    JOB_INVALID_STATUS_DELETE(3002, "Job cannot be deleted in its current status", HttpStatus.BAD_REQUEST),
    LOCATION_REQUIRED(3003, "Location is required", HttpStatus.BAD_REQUEST),
    INVALID_COORDINATES(3004, "Invalid latitude or longitude. Latitude must be between -90 and 90, longitude must be between -180 and 180", HttpStatus.BAD_REQUEST),
    GEOCODING_FAILED(3005, "Cannot determine coordinates from location. Please provide valid location or coordinates", HttpStatus.BAD_REQUEST),

    CATEGORY_NOT_FOUND(3100, "Category not found", HttpStatus.NOT_FOUND),

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
    FILE_NOT_FOUND(5002, "File not found", HttpStatus.NOT_FOUND),
    FILE_TYPE_NOT_ALLOWED(5003, "File type not allowed", HttpStatus.BAD_REQUEST),

    // ========== 6xxx: RATING ==========
    ALREADY_EXISTS(6001, "User has already rated this user for the specified job.", HttpStatus.CONFLICT),
    CANNOT_RATE_SELF(6002, "Users cannot rate themselves.", HttpStatus.BAD_REQUEST),
    RATING_NOT_FOUND(6003, "Rating not found", HttpStatus.NOT_FOUND),
    RATING_NOT_ALLOWED(6004, "Rating is not allowed for this user or job", HttpStatus.BAD_REQUEST),
    RATING_NOT_ALLOWED_BEFORE_DEADLINE(6005, "Rating is not allowed before the job completion deadline",
            HttpStatus.BAD_REQUEST),

    REPORT_NOT_FOUND(7001, "Report not found", HttpStatus.NOT_FOUND),
    REPORT_ALREADY_SUBMITTED(7002, "You have already submitted a report for this item.", HttpStatus.CONFLICT),
    REPORTER_TOO_NEW(7003, "Your account is too new to submit reports. Please try again later.", HttpStatus.FORBIDDEN),

    // ========== 8xxx: AUTH / ACCOUNT STATUS ==========
    USER_BANNED(8001,
            "Tài khoản của bạn đã bị khóa do vi phạm tiêu chuẩn cộng đồng. Vui lòng liên hệ hỗ trợ nếu bạn cho rằng đây là nhầm lẫn.",
            HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(8002, "User account is not verified", HttpStatus.FORBIDDEN),
    ALREADY_EMPLOYER(8003, "User is already an employer", HttpStatus.BAD_REQUEST),
    TOKEN_SIGN_FAILED(8004, "Cannot create token", HttpStatus.INTERNAL_SERVER_ERROR),

    // ========= 9xxx: INVITATION ==========
    INVITE_ALREADY_SENT(9000, "Đã mời ứng viên này cho công việc này rồi", HttpStatus.CONFLICT),
    INVITATION_NOT_FOUND(9001, "Invitation not found", HttpStatus.NOT_FOUND),
    INVITATION_ALREADY_PROCESSED(9002, "Invitation already processed", HttpStatus.BAD_REQUEST),

    // ========= 10xxx: WAITING LIST ==========
    WAITING_LIST_NOT_FOUND(10000, "Waiting list not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_HAS_ACTIVE_WAITING_LIST(10001, "User already has an active waiting list", HttpStatus.BAD_REQUEST),
    WAITING_LIST_HAS_PENDING_INVITATIONS(10002, "Không thể xóa waiting list vì đang có lời mời đang chờ xử lý",
            HttpStatus.CONFLICT);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
