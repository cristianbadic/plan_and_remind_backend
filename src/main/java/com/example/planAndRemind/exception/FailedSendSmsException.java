package com.example.planAndRemind.exception;

public class FailedSendSmsException extends RuntimeException {
    public FailedSendSmsException(String message) {
        super(message);
    }
}
