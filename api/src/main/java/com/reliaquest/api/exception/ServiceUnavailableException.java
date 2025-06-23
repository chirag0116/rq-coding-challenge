package com.reliaquest.api.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable ex) {
        super(message, ex);
    }
}
