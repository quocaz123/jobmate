package com.quokka.jobmate_connect.exception;

import com.quokka.jobmate_connect.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Lỗi nghiệp vụ (AppException)
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<?>> handleAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        // Sử dụng custom message nếu có, nếu không thì dùng message mặc định từ ErrorCode
        String message = exception.getMessage() != null && !exception.getMessage().equals(errorCode.getMessage())
                ? exception.getMessage()
                : errorCode.getMessage();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode, message));
    }

    //Lỗi validation từ @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException exception){
        String message = exception.getFieldError().getDefaultMessage();
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatusCode())
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, message));
    }

    //Lỗi không có quyền truy cập
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException exception){
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatusCode())
                .body(ApiResponse.error(ErrorCode.FORBIDDEN));
    }

    //Các lỗi từ hệ tống không mong đợi
    @ExceptionHandler(Exception.class)
    public  ResponseEntity<ApiResponse<?>> handleException(Exception exception){
        exception.printStackTrace();
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatusCode())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }


}
