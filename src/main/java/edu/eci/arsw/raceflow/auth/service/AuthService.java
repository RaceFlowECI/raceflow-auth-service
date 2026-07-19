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

/** Implements registration and login: password hashing, JWT issuance, and metrics. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthMetrics authMetrics;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * @param userRepository persistence for {@link User} rows
     * @param jwtService     issues and validates JWTs
     * @param authMetrics    records registration/login counters
     */
    public AuthService(UserRepository userRepository, JwtService jwtService, AuthMetrics authMetrics) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authMetrics = authMetrics;
    }

    /**
     * Registers a new user: hashes the password with BCrypt, persists the
     * user, and issues a JWT.
     *
     * @param req registration payload
     * @return the issued token and basic profile
     * @throws EmailAlreadyExistsException if the email is already registered
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
     * Authenticates a user by verifying the password against the stored
     * BCrypt hash, and issues a fresh JWT on success.
     *
     * @param req login payload
     * @return the issued token and basic profile
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if the email is unknown or the password does not match
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
     * @param email the authenticated caller's email (from the JWT subject)
     * @return the caller's profile
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
