package edu.eci.arsw.raceflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload para enviar una solicitud de amistad: el email del atleta destino. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {

    @NotBlank
    @Email
    private String email;
}
