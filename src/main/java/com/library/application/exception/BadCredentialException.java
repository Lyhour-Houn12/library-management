package com.library.application.exception;

import org.springframework.security.core.AuthenticationException;

public class BadCredentialException extends AuthenticationException {
    private static final long serialVersionUID = 3423423532L;

    public BadCredentialException(String message) {
        super(message);
    }
}
