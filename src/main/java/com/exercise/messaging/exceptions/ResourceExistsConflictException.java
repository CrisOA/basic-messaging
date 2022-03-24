package com.exercise.messaging.exceptions;


public class ResourceExistsConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ResourceExistsConflictException(String msg) {
        super(msg);
    }
}