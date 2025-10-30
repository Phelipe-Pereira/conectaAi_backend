package com.conectaai.exception;

public class InsufficientRefundableAmountException extends RuntimeException {
    public InsufficientRefundableAmountException(String message) {
        super(message);
    }
}

