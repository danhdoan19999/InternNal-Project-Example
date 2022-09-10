package com.nals.rw360.errors;

import org.zalando.problem.AbstractThrowableProblem;

import java.util.Collections;
import java.util.Optional;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

public class InternalServerErrorException
    extends AbstractThrowableProblem {

    public InternalServerErrorException(final String message) {
        super(DEFAULT_TYPE, message, INTERNAL_SERVER_ERROR, null, null, null,
              Collections.singletonMap("error_code", ErrorCodes.INTERNAL_SERVER));
    }

    public InternalServerErrorException(final String message, final String errorCode) {
        super(DEFAULT_TYPE, message, INTERNAL_SERVER_ERROR, null, null, null,
              Collections.singletonMap("error_code", Optional.ofNullable(errorCode)
                                                             .orElse(ErrorCodes.INTERNAL_SERVER)));
    }
}
