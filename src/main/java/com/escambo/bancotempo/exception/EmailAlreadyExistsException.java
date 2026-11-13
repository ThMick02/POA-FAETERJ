package com.escambo.bancotempo.exception;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
