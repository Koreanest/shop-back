package com.hbk.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;

    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}