package com.nals.rw360.errors;

import static com.nals.rw360.errors.ErrorCodes.INVALID_RESET_KEY;

public class InvalidResetKeyException
    extends ValidatorException {

    public InvalidResetKeyException() {
        super("Invalid reset key", "reset_key", INVALID_RESET_KEY);
    }
}
