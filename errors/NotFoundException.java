package com.nals.rw360.errors;

import lombok.Getter;

import java.util.List;

import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.errors.ErrorType.RW_NOT_FOUND;

@Getter
public class NotFoundException
    extends RuntimeException {
    private final List<ErrorProblem> errors;

    public NotFoundException(final List<ErrorProblem> errors) {
        super(RW_NOT_FOUND.getMessage());
        this.errors = errors;
    }

    public NotFoundException(final String message) {
        super(RW_NOT_FOUND.getMessage());

        var error = new ErrorProblem();
        error.setMessage(message);
        error.setErrorCode(OBJECT_NOT_FOUND);

        this.errors = List.of(error);
    }

    public NotFoundException(final String message, final String fieldName) {
        super(RW_NOT_FOUND.getMessage());

        var error = new ErrorProblem();
        error.setField(fieldName);
        error.setMessage(message);
        error.setErrorCode(OBJECT_NOT_FOUND);

        this.errors = List.of(error);
    }

    public NotFoundException(final Throwable cause, final String fieldName) {
        super(cause);

        var error = new ErrorProblem();
        error.setField(fieldName);
        error.setMessage(cause.getMessage());
        error.setErrorCode(OBJECT_NOT_FOUND);

        this.errors = List.of(error);
    }

    public NotFoundException(final String message, final String fieldName, final String errorCode) {
        super(RW_NOT_FOUND.getMessage());

        var error = new ErrorProblem();
        error.setField(fieldName);
        error.setMessage(message);
        error.setErrorCode(errorCode);

        this.errors = List.of(error);
    }

    public NotFoundException(final String message, final Throwable cause, final String fieldName,
                             final String errorCode) {
        super(RW_NOT_FOUND.getMessage(), cause);

        var error = new ErrorProblem();
        error.setField(fieldName);
        error.setMessage(message);
        error.setErrorCode(errorCode);

        this.errors = List.of(error);
    }

    public NotFoundException(final Throwable cause, final String fieldName, final String errorCode) {
        super(cause);

        var error = new ErrorProblem();
        error.setField(fieldName);
        error.setMessage(cause.getMessage());
        error.setErrorCode(errorCode);

        this.errors = List.of(error);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        String s = getClass().getName() + "[" + RW_NOT_FOUND.getMessage() + "]";
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
