package edu.eci.arsw.raceflow.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the bean-validation constraints on {@link RegisterRequest}
 * directly (the same {@link Validator} Spring wires up for {@code @Valid}),
 * since the controller/service unit tests call the service layer directly
 * and never trigger these annotations.
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RegisterRequest validRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("Str0ng!Pass");
        req.setName("Juan");
        req.setSport("running");
        return req;
    }

    @Test
    void acceptsAWellFormedRequest() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",       // < 8 chars
            "alllowercase1!", // no uppercase
            "ALLUPPERCASE1!", // no lowercase
            "NoDigitsHere!",  // no digit
            "NoSpecialChar1", // no special character
    })
    void rejectsPasswordsMissingAComplexityRequirement(String weakPassword) {
        RegisterRequest req = validRequest();
        req.setPassword(weakPassword);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void rejectsPasswordEqualToTheFullEmail() {
        RegisterRequest req = validRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("juan@raceflow.dev");

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void rejectsPasswordEqualToTheEmailLocalPart() {
        RegisterRequest req = validRequest();
        req.setEmail("juanperez@raceflow.dev");
        req.setPassword("JuanPerez"); // case-insensitive match against the local part

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void rejectsPasswordContainingTheEmailLocalPart() {
        RegisterRequest req = validRequest();
        req.setEmail("juanperez@raceflow.dev");
        req.setPassword("JuanPerez!2026");

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void doesNotFalsePositiveOnAShortUnrelatedLocalPart() {
        // local part "jp" is too short to meaningfully match as a substring
        RegisterRequest req = validRequest();
        req.setEmail("jp@raceflow.dev");
        req.setPassword("Str0ng!Pass");

        assertThat(validator.validate(req)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-an-email",
            "missing-tld@localhost",
            "@raceflow.dev",
            "juan@raceflow",
    })
    void rejectsMalformedEmails(String badEmail) {
        RegisterRequest req = validRequest();
        req.setEmail(badEmail);

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void acceptsARealisticEmailWithASubdomain() {
        RegisterRequest req = validRequest();
        req.setEmail("juan.perez@students.escuelaing.edu.co");

        assertThat(validator.validate(req)).isEmpty();
    }
}
