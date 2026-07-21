package edu.eci.arsw.raceflow.auth.dto;

import edu.eci.arsw.raceflow.auth.validation.PasswordNotEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Registration payload: credentials, display name, and an optional sport. */
@Data
@PasswordNotEmail
public class RegisterRequest {

    // @Email alone accepts things like "user@localhost" (no TLD); the extra
    // regexp requires a real domain suffix so only realistic addresses pass.
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$", message = "must be a real email address (user@domain.tld)")
    @NotBlank
    private String email;

    // Requires: 8+ chars, at least one lowercase, one uppercase, one digit,
    // and one special character. Cross-checked against the email separately
    // by @PasswordNotEmail, since that needs both fields at once.
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "must be at least 8 characters and include an uppercase letter, "
                    + "a lowercase letter, a number, and a special character"
    )
    @NotBlank
    private String password;

    @NotBlank
    private String name;

    private String sport;
}
