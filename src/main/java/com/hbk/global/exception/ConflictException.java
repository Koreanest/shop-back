package com.hbk.global.exception;

public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}