package com.efrei.usermicroservice.exceptions.custom;

public class EmailAlreadyExistingException extends RuntimeException {
    public EmailAlreadyExistingException(String message) {
        super(message);
    }
}
