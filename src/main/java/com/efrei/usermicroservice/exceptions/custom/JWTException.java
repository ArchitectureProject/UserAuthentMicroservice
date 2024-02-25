package com.efrei.usermicroservice.exceptions.custom;

public class JWTException extends RuntimeException {
    public JWTException(String message) {
        super(message);
    }
    public JWTException(String message, Exception e) {
        super(message, e);
    }
}