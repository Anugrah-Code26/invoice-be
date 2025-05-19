package com.invoice.backend.common.exceptions;

public class AlreadyMainException extends RuntimeException {
    public AlreadyMainException(String message) {
        super(message);
    }
}
