package edu.eci.arsw.raceflow.auth.service;

import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.LoginRequest;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.exception.EmailAlreadyExistsException;
import edu.eci.arsw.raceflow.auth.metrics.AuthMetrics;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** Implementa registro y login: hash de contrasena, emision de JWT, y metricas. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthMetrics authMetrics;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * @param userRepository persistencia para filas de {@link User}
     * @param jwtService     emite y valida JWTs
     * @param authMetrics    registra contadores de registro/login
     */
    public AuthService(UserRepository userRepository, JwtService jwtService, AuthMetrics authMetrics) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authMetrics = authMetrics;
    }

    /**
     * Registra un nuevo usuario: aplica hash BCrypt a la contrasena, persiste
     * el usuario, y emite un JWT.
     *
     * @param req payload de registro
     * @return el token emitido y el perfil basico
     * @throws EmailAlreadyExistsException si el email ya esta registrado
     */
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .sport(req.getSport())
                .build();
        userRepository.save(user);

        authMetrics.recordRegistration();

        String token = jwtService.generateToken(user.getEmail(), user.getName());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * Autentica a un usuario verificando la contrasena contra el hash
     * BCrypt almacenado, y emite un nuevo JWT si tiene exito.
     *
     * @param req payload de login
     * @return el token emitido y el perfil basico
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         si el email es desconocido o la contrasena no coincide
     */
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    authMetrics.recordLoginFailure();
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            authMetrics.recordLoginFailure();
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getName());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * @param email el email del solicitante autenticado (del subject del JWT)
     * @return el perfil del solicitante
     */
    public UserProfileResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .sport(user.getSport())
                .build();
    }
}
