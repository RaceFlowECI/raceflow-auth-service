package edu.eci.arsw.raceflow.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Amistad entre dos usuarios, identificada por email como el resto del sistema.
 * Una sola fila representa tanto la solicitud (PENDING) como, una vez aceptada,
 * la amistad bidireccional (ACCEPTED).
 */
@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(
        columnNames = {"requesterEmail", "addresseeEmail"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    public enum Status { PENDING, ACCEPTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requesterEmail;

    @Column(nullable = false)
    private String addresseeEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
