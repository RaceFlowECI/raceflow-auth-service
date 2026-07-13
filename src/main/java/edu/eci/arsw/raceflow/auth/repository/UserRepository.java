package edu.eci.arsw.raceflow.auth.repository;

import edu.eci.arsw.raceflow.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data para filas de {@link User}. */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param email el email a buscar
     * @return el usuario encontrado, si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busqueda sin distincion de mayusculas por subcadena de email o nombre, usada por
     * el endpoint de busqueda de amigos.
     *
     * @param email subcadena comparada contra el email
     * @param name  subcadena comparada contra el nombre (mismo valor que {@code email})
     * @return maximo 10 coincidencias
     */
    List<User> findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);

    /**
     * @param email el email a verificar
     * @return {@code true} si ya existe un usuario registrado con ese email
     */
    boolean existsByEmail(String email);
}
