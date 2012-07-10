package com.agiledirigible.droidvalidate.annotations;

import com.agiledirigible.droidvalidate.DVProcessor;
import com.agiledirigible.droidvalidate.helper.ValidationMethodNames;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TextViewConstraint {

    @DVConstraint(ValidationMethodNames.VALIDATION_METHOD_NOT_EMPTY)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface NotEmpty {
    }

    @DVConstraint(ValidationMethodNames.VALIDATION_METHOD_MATCHES)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Matches {
        int value();
    }

    @DVConstraint(ValidationMethodNames.VALIDATION_METHOD_LENGTH)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Length {
        int minLength() default -1;
        int maxLength() default -1;
    }
}
