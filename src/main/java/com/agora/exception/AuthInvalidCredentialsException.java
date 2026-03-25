package com.agora.exception;

public class AuthInvalidCredentialsException extends RuntimeException {

    public AuthInvalidCredentialsException() {
        super("Email ou mot de passe invalide");
    }
}
