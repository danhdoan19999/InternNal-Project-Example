package com.nals.rw360.errors.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Repeatable(ErrorMappings.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorMapping {
    Class<? extends Annotation> value();

    String code() default "";
}
