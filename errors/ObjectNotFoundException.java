package com.nals.rw360.errors;

import java.util.Optional;

public class ObjectNotFoundException
    extends NotFoundException {

    public ObjectNotFoundException(final String fieldName) {
        super("Object not found", fieldName, ErrorCodes.OBJECT_NOT_FOUND);
    }

    public ObjectNotFoundException(final String fieldName, final String errorCode) {
        super("Object not found", fieldName, Optional.ofNullable(errorCode).orElse(ErrorCodes.OBJECT_NOT_FOUND));
    }

    public ObjectNotFoundException(final String message, final String fieldName, final String errorCode) {
        super(message, fieldName, Optional.ofNullable(errorCode).orElse(ErrorCodes.OBJECT_NOT_FOUND));
    }
}
