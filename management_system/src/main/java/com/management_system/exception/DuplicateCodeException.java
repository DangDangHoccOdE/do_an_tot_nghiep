package com.management_system.exception;

/**
 * Exception when code is duplicated in DB.
 */
public class DuplicateCodeException extends RuntimeException {

    public DuplicateCodeException(String message) {
        super(message);
    }

    public DuplicateCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}

