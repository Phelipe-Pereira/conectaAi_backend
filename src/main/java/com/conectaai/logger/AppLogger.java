package com.conectaai.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {
    
    private static final String LOG_FORMAT = "{}.{} >> {}";
    
    private final Logger log;
    private final String className;
    
    private AppLogger(Class<?> clazz) {
        this.log = LoggerFactory.getLogger(clazz);
        this.className = clazz.getSimpleName();
    }
    
    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }
    
    public void info(String methodName, String message) {
        log.info(LOG_FORMAT, className, methodName, message);
    }
    
    public void info(String methodName, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.info(LOG_FORMAT, className, methodName, formattedMessage);
    }
    
    public void warn(String methodName, String message) {
        log.warn(LOG_FORMAT, className, methodName, message);
    }
    
    public void warn(String methodName, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.warn(LOG_FORMAT, className, methodName, formattedMessage);
    }
    
    public void error(String methodName, String message) {
        log.error(LOG_FORMAT, className, methodName, message);
    }
    
    public void error(String methodName, String message, Throwable throwable) {
        log.error(LOG_FORMAT, className, methodName, message, throwable);
    }
    
    public void error(String methodName, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.error(LOG_FORMAT, className, methodName, formattedMessage);
    }
    
    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", arg != null ? arg.toString() : "null");
        }
        return result;
    }
}

