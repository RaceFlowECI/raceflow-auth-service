package edu.eci.arsw.raceflow.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Friendship between two users, keyed by email like the rest of the system.
 * A single row represents both the request (PENDING) and, once accepted,
 * the bidirectional friendship (ACCEPTED).
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
