package com.nals.rw360.errors;

import java.net.URI;

public enum ErrorType {
    RW_CONCURRENCY_FAILURE("error.concurrency", "/concurrency"),
    RW_SECURITY("error.security", "/security"),
    RW_HTTP("error.http", "/http"),
    RW_VALIDATION("error.validation", "/constraint-violation"),
    RW_NOT_FOUND("error.not_found", "/object-not-found");

    private static final String PROBLEM_BASE_URL = ""; // TODO change it
    private final String message;
    private final URI type;

    ErrorType(final String message, final String type) {
        this.message = message;
        this.type = URI.create(PROBLEM_BASE_URL + type);
    }

    public String getMessage() {
        return message;
    }

    public URI getType() {
        return type;
    }
}
