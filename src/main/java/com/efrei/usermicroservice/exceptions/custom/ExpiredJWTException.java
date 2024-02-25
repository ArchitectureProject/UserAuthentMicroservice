package com.efrei.usermicroservice.exceptions.custom;

public class ExpiredJWTException extends RuntimeException {
    public ExpiredJWTException(String message) {
        super(message);
    }
}