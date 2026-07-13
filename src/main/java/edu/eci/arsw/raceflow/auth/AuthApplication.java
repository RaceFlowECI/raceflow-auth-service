package edu.eci.arsw.raceflow.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Auth Service. Maneja el registro de usuarios, login,
 * emision de JWT, amistades, y expone el servicio gRPC interno UserProfileService
 * que consume realtime-service.
 */
@SpringBootApplication
public class AuthApplication {

    /**
     * Arranca el contexto de la aplicacion Spring.
     *
     * @param args argumentos de linea de comandos pasados a Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
