package com.conectaai.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    
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
        log.info("{}.{} >> {}", className, methodName, message);
    }
    
    public void info(String methodName, String message, Object... args) {
        log.info("{}.{} >> " + message, prependArgs(className, methodName, args));
    }
    
    public void warn(String methodName, String message) {
        log.warn("{}.{} >> {}", className, methodName, message);
    }
    
    public void warn(String methodName, String message, Object... args) {
        log.warn("{}.{} >> " + message, prependArgs(className, methodName, args));
    }
    
    public void error(String methodName, String message) {
        log.error("{}.{} >> {}", className, methodName, message);
    }
    
    public void error(String methodName, String message, Throwable throwable) {
        log.error("{}.{} >> {}", className, methodName, message, throwable);
    }
    
    public void error(String methodName, String message, Object... args) {
        log.error("{}.{} >> " + message, prependArgs(className, methodName, args));
    }
    
    private Object[] prependArgs(String className, String methodName, Object... args) {
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = className;
        newArgs[1] = methodName;
        System.arraycopy(args, 0, newArgs, 2, args.length);
        return newArgs;
    }
}

