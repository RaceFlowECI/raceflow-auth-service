package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data repository for {@link User} rows. */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param email the email to look up
     * @return the matching user, if any
     */
    Optional<User> findByEmail(String email);

    /**
     * Case-insensitive search by email or name substring, used by the
     * friend-search endpoint.
     *
     * @param email substring matched against email
     * @param name  substring matched against name (same value as {@code email})
     * @return at most 10 matches
     */
    List<User> findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);

    /**
     * @param email the email to check
     * @return {@code true} if a user with that email is already registered
     */
    boolean existsByEmail(String email);
}
