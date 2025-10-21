package com.conectaai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserInactiveException extends RuntimeException {
    
    public UserInactiveException(String message) {
        super(message);
    }
    
    public UserInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

