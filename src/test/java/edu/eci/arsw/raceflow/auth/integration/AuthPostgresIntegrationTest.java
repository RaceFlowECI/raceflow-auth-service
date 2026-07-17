package edu.eci.arsw.raceflow.auth.integration;

import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.LoginRequest;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.exception.EmailAlreadyExistsException;
import edu.eci.arsw.raceflow.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test against a real PostgreSQL instance in Docker (Testcontainers),
 * not the fast H2 in-memory database the rest of the suite uses. Exercises the
 * full register/login flow -- password hashing, unique-email constraint, JWT
 * issuance -- with the exact database engine used in production.
 */
@Testcontainers
@SpringBootTest
class AuthPostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("auth")
            .withUsername("raceflow")
            .withPassword("secret");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Test
    void shouldRegisterAndLoginAgainstRealPostgres() {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("integration@raceflow.dev");
        register.setPassword("Sup3rSecret");
        register.setName("Integration Test");

        AuthResponse registered = authService.register(register);
        assertNotNull(registered.getToken());
        assertEquals("integration@raceflow.dev", registered.getEmail());

        LoginRequest login = new LoginRequest();
        login.setEmail("integration@raceflow.dev");
        login.setPassword("Sup3rSecret");

        AuthResponse loggedIn = authService.login(login);
        assertNotNull(loggedIn.getToken());
        assertEquals("integration@raceflow.dev", loggedIn.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmailAgainstRealPostgres() {
        RegisterRequest first = new RegisterRequest();
        first.setEmail("duplicate@raceflow.dev");
        first.setPassword("Sup3rSecret");
        first.setName("First");
        authService.register(first);

        RegisterRequest second = new RegisterRequest();
        second.setEmail("duplicate@raceflow.dev");
        second.setPassword("Other1234");
        second.setName("Second");

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(second));
    }

    @Test
    void shouldRejectLoginWithWrongPasswordAgainstRealPostgres() {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("wrongpass@raceflow.dev");
        register.setPassword("Correct123");
        register.setName("Wrong Pass");
        authService.register(register);

        LoginRequest login = new LoginRequest();
        login.setEmail("wrongpass@raceflow.dev");
        login.setPassword("Incorrect999");

        assertThrows(BadCredentialsException.class, () -> authService.login(login));
    }
}
