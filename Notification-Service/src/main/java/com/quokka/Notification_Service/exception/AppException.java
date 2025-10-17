package com.quokka.Notification_Service.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException{

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

   public  AppException(String message, ErrorCode errorCode){
        super(message);
        this.errorCode = errorCode;
    }
}
