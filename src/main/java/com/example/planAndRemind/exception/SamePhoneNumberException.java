package com.example.planAndRemind.exception;

public class SamePhoneNumberException extends RuntimeException {
    public SamePhoneNumberException(String message) {
        super(message);
    }
}
