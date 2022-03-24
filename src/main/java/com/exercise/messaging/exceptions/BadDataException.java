package com.exercise.messaging.exceptions;


public class BadDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BadDataException(String msg) {
        super(msg);
    }
}
