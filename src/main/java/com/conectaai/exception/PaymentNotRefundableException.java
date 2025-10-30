package com.conectaai.exception;

public class PaymentNotRefundableException extends RuntimeException {
    public PaymentNotRefundableException(String message) {
        super(message);
    }
}

