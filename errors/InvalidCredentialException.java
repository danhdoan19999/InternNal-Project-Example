package com.nals.rw360.errors;

import static com.nals.rw360.errors.ErrorCodes.INVALID_USERNAME_OR_PASSWORD;

public class InvalidCredentialException
    extends ValidatorException {

    public InvalidCredentialException(final Integer limitTryNumber, final Integer remainRetryNumber) {
        super();

        var error = new ErrorProblem();
        error.setMessage("Invalid username or password");
        error.setErrorCode(INVALID_USERNAME_OR_PASSWORD);
        error.setLimitTryNumber(limitTryNumber);
        error.setRemainRetryNumber(remainRetryNumber);

        addError(error);
    }
}
