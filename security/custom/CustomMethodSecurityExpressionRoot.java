package com.nals.rw360.security.custom;

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Base on MethodSecurityExpressionRoot
 * {@link org.springframework.security.access.expression.method.MethodSecurityExpressionRoot}
 */
public class CustomMethodSecurityExpressionRoot
    extends CustomSecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

    private Object filterObject;

    private Object returnObject;

    private Object target;

    CustomMethodSecurityExpressionRoot(final Authentication authentication) {
        super(authentication);
    }

    @Override
    public void setFilterObject(final Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(final Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    /**
     * Sets the "this" property for use in expressions. Typically this will be the "this"
     * property of the {@code JoinPoint} representing the method invocation which is being
     * protected.
     *
     * @param target the target object on which the method in is being invoked.
     */
    void setThis(final Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return this.target;
    }
}
