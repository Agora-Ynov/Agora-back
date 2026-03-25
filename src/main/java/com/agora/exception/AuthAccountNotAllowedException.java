package com.agora.exception;

public class AuthAccountNotAllowedException extends RuntimeException {

    public AuthAccountNotAllowedException(String message) {
        super(message);
    }
}
