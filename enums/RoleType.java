package com.nals.rw360.enums;

import com.nals.rw360.errors.ObjectNotFoundException;

import java.util.Arrays;

import static com.nals.rw360.errors.ErrorCodes.ROLE_TYPE_NOT_FOUND;

public enum RoleType {
    ROLE_ADMIN, ROLE_MANAGER, ROLE_ACCOUNT_LEADER, ROLE_PMO, ROLE_MEMBER;

    public static RoleType get(final String value) {
        return Arrays.stream(values())
                     .filter(roleType -> roleType.name().equals(value))
                     .findFirst()
                     .orElseThrow(() -> new ObjectNotFoundException("role_type", ROLE_TYPE_NOT_FOUND));
    }
}
