package com.conectaai.exception;

public class InvalidSubscriptionStatusTransitionException extends RuntimeException {
    public InvalidSubscriptionStatusTransitionException(String message) {
        super(message);
    }
}

