package com.Expenzo_Backend.Expenzo.exception;

public class BankAccountDoesNotExistsException extends RuntimeException {
    public BankAccountDoesNotExistsException(String message) {
        super(message);
    }
}
