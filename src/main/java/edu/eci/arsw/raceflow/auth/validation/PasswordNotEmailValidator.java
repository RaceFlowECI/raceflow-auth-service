package edu.eci.arsw.raceflow.auth.validation;

import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates {@link PasswordNotEmail}. Field-format checks ({@code @Email},
 * password strength) run independently via their own annotations; this only
 * checks the relationship between the two fields, so it needs class-level
 * access to both.
 */
public class PasswordNotEmailValidator implements ConstraintValidator<PasswordNotEmail, RegisterRequest> {

    // Below this length the local part is too generic (e.g. "a", "jo") to be a
    // meaningful substring match -- flagging it would reject unrelated passwords.
    private static final int MIN_LOCAL_PART_FOR_SUBSTRING_CHECK = 3;

    @Override
    public boolean isValid(RegisterRequest req, ConstraintValidatorContext context) {
        if (req == null || req.getEmail() == null || req.getPassword() == null) {
            return true; // let @NotBlank/@Email report missing-field errors instead
        }

        String email = req.getEmail().trim().toLowerCase();
        String password = req.getPassword().trim().toLowerCase();
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        boolean matchesEmail = password.equals(email) || password.equals(localPart)
                || (localPart.length() >= MIN_LOCAL_PART_FOR_SUBSTRING_CHECK && password.contains(localPart));

        if (!matchesEmail) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("password")
                .addConstraintViolation();
        return false;
    }
}
