package edu.eci.arsw.raceflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload to send a friend request: the target athlete's email. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {

    @NotBlank
    @Email
    private String email;
}
