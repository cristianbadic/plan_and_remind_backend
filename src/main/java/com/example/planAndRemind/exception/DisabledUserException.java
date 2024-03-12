package com.example.planAndRemind.exception;

public class DisabledUserException extends RuntimeException{
    public DisabledUserException (String message){
        super(message);
    }
}
