package edu.eci.arsw.raceflow.auth.service;

import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.LoginRequest;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.exception.EmailAlreadyExistsException;
import edu.eci.arsw.raceflow.auth.metrics.AuthMetrics;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthMetrics authMetrics;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authMetrics = new AuthMetrics(new SimpleMeterRegistry());
        authService = new AuthService(userRepository, jwtService, authMetrics);
    }

    @Test
    void registerHashesPasswordSavesUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("secret123");
        req.setName("Juan");
        req.setSport("ciclismo");

        when(userRepository.existsByEmail("juan@raceflow.dev")).thenReturn(false);
        when(jwtService.generateToken("juan@raceflow.dev", "Juan")).thenReturn("signed.jwt.token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("signed.jwt.token");
        assertThat(response.getEmail()).isEqualTo("juan@raceflow.dev");
        assertThat(response.getName()).isEqualTo("Juan");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("juan@raceflow.dev");
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(new BCryptPasswordEncoder().matches("secret123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("secret123");
        req.setName("Juan");

        when(userRepository.existsByEmail("juan@raceflow.dev")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        String hash = new BCryptPasswordEncoder().encode("secret123");
        User user = User.builder().email("juan@raceflow.dev").passwordHash(hash).name("Juan").build();

        LoginRequest req = new LoginRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("secret123");

        when(userRepository.findByEmail("juan@raceflow.dev")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("juan@raceflow.dev", "Juan")).thenReturn("signed.jwt.token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("signed.jwt.token");
    }

    @Test
    void loginThrowsBadCredentialsWhenUserDoesNotExist() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@raceflow.dev");
        req.setPassword("whatever");

        when(userRepository.findByEmail("ghost@raceflow.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordDoesNotMatch() {
        String hash = new BCryptPasswordEncoder().encode("correct-password");
        User user = User.builder().email("juan@raceflow.dev").passwordHash(hash).name("Juan").build();

        LoginRequest req = new LoginRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("wrong-password");

        when(userRepository.findByEmail("juan@raceflow.dev")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void meReturnsProfileForExistingUser() {
        User user = User.builder().email("juan@raceflow.dev").passwordHash("hash").name("Juan").sport("ciclismo").build();
        when(userRepository.findByEmail("juan@raceflow.dev")).thenReturn(Optional.of(user));

        UserProfileResponse profile = authService.me("juan@raceflow.dev");

        assertThat(profile.getEmail()).isEqualTo("juan@raceflow.dev");
        assertThat(profile.getName()).isEqualTo("Juan");
        assertThat(profile.getSport()).isEqualTo("ciclismo");
    }

    @Test
    void meThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("ghost@raceflow.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me("ghost@raceflow.dev"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
