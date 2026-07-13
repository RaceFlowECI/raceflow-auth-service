package edu.eci.arsw.raceflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A friend request pending the caller's response, with the requester's resolved name. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRequestResponse {
    private Long id;
    private String fromEmail;
    private String fromName;
}
