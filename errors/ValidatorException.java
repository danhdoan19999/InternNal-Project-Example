package com.nals.rw360.errors;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.nals.rw360.errors.ErrorCodes.BAD_REQUEST;

@Getter
@Setter
public class ValidatorException
    extends RuntimeException {

    private final List<ErrorProblem> errors;

    protected ValidatorException() {
        super();
        this.errors = new ArrayList<>();
    }

    public ValidatorException(final List<ErrorProblem> errors) {
        super(ErrorType.RW_VALIDATION.getMessage());
        this.errors = errors;
    }

    public ValidatorException(final String message) {
        super(ErrorType.RW_VALIDATION.getMessage());

        var problem = new ErrorProblem();
        problem.setMessage(message);
        problem.setErrorCode(BAD_REQUEST);

        this.errors = List.of(problem);
    }

    public ValidatorException(final String message, final String fieldName) {
        super(ErrorType.RW_VALIDATION.getMessage());

        var problem = new ErrorProblem();
        problem.setField(fieldName);
        problem.setMessage(message);
        problem.setErrorCode(BAD_REQUEST);

        this.errors = List.of(problem);
    }

    public ValidatorException(final Throwable cause, final String fieldName) {
        super(cause);

        var problem = new ErrorProblem();
        problem.setField(fieldName);
        problem.setMessage(cause.getMessage());
        problem.setErrorCode(BAD_REQUEST);

        this.errors = List.of(problem);
    }

    public ValidatorException(final String message, final String fieldName, final String errorCode) {
        super(ErrorType.RW_VALIDATION.getMessage());

        var problem = new ErrorProblem();
        problem.setField(fieldName);
        problem.setMessage(message);
        problem.setErrorCode(errorCode);

        this.errors = List.of(problem);
    }

    public ValidatorException(final String message, final Throwable cause,
                              final String fieldName, final String errorCode) {
        super(ErrorType.RW_VALIDATION.getMessage(), cause);

        var problem = new ErrorProblem();
        problem.setField(fieldName);
        problem.setMessage(message);
        problem.setErrorCode(errorCode);

        this.errors = List.of(problem);
    }

    public ValidatorException(final Throwable cause, final String fieldName, final String errorCode) {
        super(cause);

        var problem = new ErrorProblem();
        problem.setField(fieldName);
        problem.setMessage(cause.getMessage());
        problem.setErrorCode(errorCode);

        this.errors = List.of(problem);
    }

    public void addError(final ErrorProblem error) {
        errors.add(error);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        String s = getClass().getName() + "[" + ErrorType.RW_VALIDATION.getMessage() + "]";
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
