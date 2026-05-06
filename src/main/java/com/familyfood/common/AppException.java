package com.familyfood.common;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AppException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public static AppException badRequest(String message) {
        return new AppException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    public static AppException unauthorized(String message) {
        return new AppException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public static AppException forbidden(String message) {
        return new AppException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static AppException notFound(String message) {
        return new AppException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public static AppException conflict(String message) {
        return new AppException("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public static AppException validation(String message) {
        return new AppException("VALIDATION_ERROR", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static AppException serviceUnavailable(String code, String message) {
        return new AppException(code, message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
