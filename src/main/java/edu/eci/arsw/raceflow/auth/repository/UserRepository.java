package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);

    boolean existsByEmail(String email);
}
