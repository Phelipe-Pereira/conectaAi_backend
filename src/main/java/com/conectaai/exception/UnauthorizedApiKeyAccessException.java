package com.conectaai.exception;

public class UnauthorizedApiKeyAccessException extends RuntimeException {
    public UnauthorizedApiKeyAccessException(String message) {
        super(message);
    }
}

