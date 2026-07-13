package edu.eci.arsw.raceflow.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Auth Service. Handles user registration, login, JWT
 * issuance, friendships, and exposes the internal gRPC UserProfileService
 * consumed by realtime-service.
 */
@SpringBootApplication
public class AuthApplication {

    /**
     * Boots the Spring application context.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
