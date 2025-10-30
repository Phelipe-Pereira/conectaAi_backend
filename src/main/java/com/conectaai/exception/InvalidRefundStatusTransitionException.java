package com.conectaai.exception;

public class InvalidRefundStatusTransitionException extends RuntimeException {
    public InvalidRefundStatusTransitionException(String message) {
        super(message);
    }
}

