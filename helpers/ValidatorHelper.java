package com.nals.rw360.helpers;

import com.nals.rw360.errors.ValidatorException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

import static com.nals.rw360.config.Constants.EMAIL_PATTERN;
import static com.nals.rw360.config.Constants.PASSWORD_MIN_LENGTH;
import static com.nals.rw360.config.Constants.PHONE_PATTERN;
import static com.nals.rw360.config.Constants.POSTAL_CODE_PATTERN;
import static com.nals.rw360.errors.ErrorCodes.BAD_REQUEST;

public final class ValidatorHelper {
    private ValidatorHelper() {
    }

    public static void validateField(final Object field, final String fieldName)
        throws ValidatorException {
        if (field == null) {
            throw new ValidatorException("Not null", fieldName, BAD_REQUEST);
        }

        if (field instanceof String && ((String) field).length() == 0) {
            throw new ValidatorException("Not blank", fieldName, BAD_REQUEST);
        }
    }

    public static void validateField(final Object field, final String fieldName, final String errorCode)
        throws ValidatorException {
        if (field == null) {
            throw new ValidatorException("Not null", fieldName, errorCode);
        }

        if (field instanceof String && ((String) field).length() == 0) {
            throw new ValidatorException("Not blank", fieldName, errorCode);
        }
    }

    public static boolean isValidEmail(final String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        return Pattern.compile(EMAIL_PATTERN)
                      .matcher(email)
                      .matches();
    }

    public static boolean isValidPassword(final String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }

        return password.length() >= PASSWORD_MIN_LENGTH;
    }

    public static boolean isValidPhone(final String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }

        return Pattern.compile(PHONE_PATTERN)
                      .matcher(phone)
                      .matches();
    }

    public static boolean isValidPostalCode(final String postalCode) {
        if (!StringUtils.hasText(postalCode)) {
            return false;
        }

        return Pattern.compile(POSTAL_CODE_PATTERN)
                      .matcher(postalCode)
                      .matches();
    }
}
