package com.example.planAndRemind.exception;

public class NoOverlapException extends RuntimeException {
    public NoOverlapException(String message){
        super(message);
    }
}
