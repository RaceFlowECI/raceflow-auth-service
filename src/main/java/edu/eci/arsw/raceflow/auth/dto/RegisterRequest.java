package edu.eci.arsw.raceflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Registration payload: credentials, display name, and an optional sport. */
@Data
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @Size(min = 8)
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    @NotBlank
    private String name;

    private String sport;
}
