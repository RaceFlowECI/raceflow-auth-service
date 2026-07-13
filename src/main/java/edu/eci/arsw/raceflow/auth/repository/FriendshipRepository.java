package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data para filas de {@link Friendship}. */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Encuentra la fila de amistad entre dos emails, sin importar quien
     * la haya solicitado.
     *
     * @param a un email
     * @param b el otro email
     * @return la fila, si existe una solicitud o amistad entre ellos
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requesterEmail = :a AND f.addresseeEmail = :b) "
            + "OR (f.requesterEmail = :b AND f.addresseeEmail = :a)")
    Optional<Friendship> findBetween(@Param("a") String a, @Param("b") String b);

    /**
     * @param addresseeEmail el destinatario de la solicitud
     * @param status         el estado por el cual filtrar
     * @return solicitudes que cumplen ambos criterios
     */
    List<Friendship> findByAddresseeEmailAndStatus(String addresseeEmail, Friendship.Status status);

    /**
     * @param email el email de un atleta
     * @return todas las amistades aceptadas donde aparezca en cualquiera de los dos lados
     */
    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' "
            + "AND (f.requesterEmail = :email OR f.addresseeEmail = :email)")
    List<Friendship> findAcceptedInvolving(@Param("email") String email);
}
