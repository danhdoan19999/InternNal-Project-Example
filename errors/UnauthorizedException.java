package com.nals.rw360.errors;

import java.util.Optional;

import static com.nals.rw360.errors.ErrorCodes.UNAUTHORIZED;

public class UnauthorizedException
    extends ValidatorException {

    public UnauthorizedException(final String message) {
        this(message, null, UNAUTHORIZED);
    }

    public UnauthorizedException(final String message, final Throwable cause) {
        this(message, cause, UNAUTHORIZED);
    }

    public UnauthorizedException(final String message, final Throwable cause, final String errorCode) {
        super(message, cause, null, Optional.ofNullable(errorCode).orElse(UNAUTHORIZED));
    }
}
