package com.Expenzo_Backend.Expenzo.exception;

public class NoCategoryExistsException extends RuntimeException {
    public NoCategoryExistsException(String message) {
        super(message);
    }
}
