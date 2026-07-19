package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Spring Data repository for {@link Friendship} rows. */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Finds the friendship row between two emails, regardless of who
     * requested it.
     *
     * @param a one email
     * @param b the other email
     * @return the row, if a request or friendship exists between them
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requesterEmail = :a AND f.addresseeEmail = :b) "
            + "OR (f.requesterEmail = :b AND f.addresseeEmail = :a)")
    Optional<Friendship> findBetween(@Param("a") String a, @Param("b") String b);

    /**
     * @param addresseeEmail the recipient of the request
     * @param status         the status to filter by
     * @return requests matching both criteria
     */
    List<Friendship> findByAddresseeEmailAndStatus(String addresseeEmail, Friendship.Status status);

    /**
     * @param email an athlete's email
     * @return all accepted friendships where they appear on either side
     */
    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' "
            + "AND (f.requesterEmail = :email OR f.addresseeEmail = :email)")
    List<Friendship> findAcceptedInvolving(@Param("email") String email);
}
