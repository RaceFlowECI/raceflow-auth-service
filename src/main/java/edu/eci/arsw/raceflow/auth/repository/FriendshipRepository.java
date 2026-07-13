package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requesterEmail = :a AND f.addresseeEmail = :b) "
            + "OR (f.requesterEmail = :b AND f.addresseeEmail = :a)")
    Optional<Friendship> findBetween(@Param("a") String a, @Param("b") String b);

    List<Friendship> findByAddresseeEmailAndStatus(String addresseeEmail, Friendship.Status status);

    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' "
            + "AND (f.requesterEmail = :email OR f.addresseeEmail = :email)")
    List<Friendship> findAcceptedInvolving(@Param("email") String email);
}
