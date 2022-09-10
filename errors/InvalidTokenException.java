package com.nals.rw360.errors;

public class InvalidTokenException
    extends ValidatorException {

    public InvalidTokenException() {
        super("Invalid refresh token", "token", ErrorCodes.INVALID_REFRESH_TOKEN);
    }
}
