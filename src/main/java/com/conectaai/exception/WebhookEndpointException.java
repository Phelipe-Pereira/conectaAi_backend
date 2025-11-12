package com.conectaai.exception;

public class WebhookEndpointException extends RuntimeException {

    public WebhookEndpointException(String message) {
        super(message);
    }

    public WebhookEndpointException(String message, Throwable cause) {
        super(message, cause);
    }
}

