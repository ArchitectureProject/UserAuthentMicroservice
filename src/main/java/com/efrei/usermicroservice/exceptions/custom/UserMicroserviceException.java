package com.efrei.usermicroservice.exceptions.custom;

public class UserMicroserviceException extends RuntimeException {
    public UserMicroserviceException(String message) {
        super(message);
    }

    public UserMicroserviceException(String message, Exception e) {
        super(message, e);
    }
}