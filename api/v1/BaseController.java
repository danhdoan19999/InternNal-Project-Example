package com.nals.rw360.api.v1;

import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
public class BaseController {

    private final Validator validator;

    public <T> void validateRequest(final T req) {
        Set<ConstraintViolation<T>> errors = validator.validate(req);
        if (!CollectionUtils.isEmpty(errors)) {
            ConstraintViolation<T> error = errors.iterator().next();
            throw new ValidatorException(error.getMessage(), error.getPropertyPath().toString());
        }
    }

    public ResponseEntity<?> ok(final Object body) {
        return ResponseEntity.ok(ResponseHelper.build(body));
    }

    public ResponseEntity<?> ok(final List<?> body) {
        return ResponseEntity.ok(ResponseHelper.build(body));
    }

    public ResponseEntity<?> ok(final Page<?> body) {
        return ResponseEntity.ok(ResponseHelper.build(body));
    }

    public ResponseEntity<?> created(final Long id) {
        return new ResponseEntity<>(ResponseHelper.build(id), CREATED);
    }

    public ResponseEntity<?> noContent() {
        return ResponseEntity.noContent().build();
    }
}
