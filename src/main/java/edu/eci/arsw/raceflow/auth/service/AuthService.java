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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthMetrics authMetrics;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService, AuthMetrics authMetrics) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authMetrics = authMetrics;
    }

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

        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

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

        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

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
