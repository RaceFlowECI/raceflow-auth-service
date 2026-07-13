package edu.eci.arsw.raceflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Una solicitud de amistad pendiente de respuesta, con el nombre del solicitante resuelto. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRequestResponse {
    private Long id;
    private String fromEmail;
    private String fromName;
}
