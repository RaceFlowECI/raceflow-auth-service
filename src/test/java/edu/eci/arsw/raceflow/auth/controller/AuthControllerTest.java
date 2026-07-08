package edu.eci.arsw.raceflow.auth.controller;

import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.LoginRequest;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthController(authService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerReturns201WithAuthResponse() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("secret123");
        req.setName("Juan");

        AuthResponse expected = AuthResponse.builder().token("t").email("juan@raceflow.dev").name("Juan").build();
        when(authService.register(req)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = controller.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void loginReturns200WithAuthResponse() {
        LoginRequest req = new LoginRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("secret123");

        AuthResponse expected = AuthResponse.builder().token("t").email("juan@raceflow.dev").name("Juan").build();
        when(authService.login(req)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = controller.login(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void meReadsEmailFromSecurityContextAndReturnsProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("juan@raceflow.dev", null));

        UserProfileResponse expected = UserProfileResponse.builder()
                .email("juan@raceflow.dev").name("Juan").sport("ciclismo").build();
        when(authService.me("juan@raceflow.dev")).thenReturn(expected);

        ResponseEntity<UserProfileResponse> response = controller.me();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }
}
