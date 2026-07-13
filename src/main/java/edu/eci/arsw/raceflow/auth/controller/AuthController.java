package edu.eci.arsw.raceflow.auth.controller;

import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.LoginRequest;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para registro, login, y el perfil propio del usuario autenticado.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * @param authService servicio que implementa la logica de registro/login
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra un nuevo atleta.
     *
     * @param req email, contrasena, nombre y deporte opcional
     * @return {@code 201 Created} con el JWT emitido y el perfil basico
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    /**
     * Autentica a un atleta existente.
     *
     * @param req email y contrasena
     * @return {@code 200 OK} con un nuevo JWT y el perfil basico
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * Retorna el perfil del atleta actualmente autenticado, resuelto
     * a partir del subject del JWT establecido por {@link edu.eci.arsw.raceflow.auth.config.JwtAuthFilter}.
     *
     * @return el perfil del solicitante
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(authService.me(email));
    }
}
