package com.conectaai.exception;

import com.conectaai.enums.Provider;

public class GatewayException extends RuntimeException {

    private final Provider provider;

    public GatewayException(Provider provider, String message) {
        super(message);
        this.provider = provider;
    }

    public GatewayException(Provider provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }
}

