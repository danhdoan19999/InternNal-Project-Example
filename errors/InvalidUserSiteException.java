package com.nals.rw360.errors;

import static com.nals.rw360.errors.ErrorCodes.INVALID_USER_SITE;

public class InvalidUserSiteException
    extends ValidatorException {

    public InvalidUserSiteException() {
        super();

        var error = new ErrorProblem();
        error.setMessage("Invalid user site");
        error.setErrorCode(INVALID_USER_SITE);

        addError(error);
    }
}
