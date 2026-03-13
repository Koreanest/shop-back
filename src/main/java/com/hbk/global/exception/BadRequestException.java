package com.hbk.global.exception;

public class BadRequestException extends BusinessException {

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}