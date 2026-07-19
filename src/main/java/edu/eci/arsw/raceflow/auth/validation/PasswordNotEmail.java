package edu.eci.arsw.raceflow.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint rejecting a password that equals, or contains, the
 * account's email address (or its local part before {@code @}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordNotEmailValidator.class)
public @interface PasswordNotEmail {

    String message() default "must not be the same as, or contain, the email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
