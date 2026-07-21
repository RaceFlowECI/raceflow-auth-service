package edu.eci.arsw.raceflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.arsw.raceflow.auth.dto.AuthResponse;
import edu.eci.arsw.raceflow.auth.dto.RegisterRequest;
import edu.eci.arsw.raceflow.auth.exception.EmailAlreadyExistsException;
import edu.eci.arsw.raceflow.auth.exception.GlobalExceptionHandler;
import edu.eci.arsw.raceflow.auth.service.AuthService;
import edu.eci.arsw.raceflow.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for {@code POST /auth/register} through the real HTTP layer
 * (MockMvc): actual JSON (de)serialization, actual status codes from Spring's
 * dispatcher, and the actual {@code @Valid} validation pipeline -- none of
 * which {@link AuthControllerTest} exercises, since it calls the controller
 * method directly as a plain Java call. Security filters are disabled here
 * on purpose: JWT auth is covered separately by {@code JwtAuthFilterTest},
 * and {@code /auth/register} is a public route anyway.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Not exercised directly (security filters are disabled) -- required only
    // because SecurityConfig/JwtAuthFilter are still on the @WebMvcTest slice
    // and need a JwtService bean to construct.
    @MockBean
    private JwtService jwtService;

    @Test
    void registerWithValidPayloadReturns201WithTokenAndProfile() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("Str0ng!Pass");
        req.setName("Juan");

        when(authService.register(any(RegisterRequest.class))).thenReturn(
                AuthResponse.builder().token("jwt-token").email("juan@raceflow.dev").name("Juan").build());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("juan@raceflow.dev"))
                .andExpect(jsonPath("$.name").value("Juan"));
    }

    @Test
    void registerWithMalformedEmailReturns400WithFieldError() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("Str0ng!Pass");
        req.setName("Juan");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields[0]").value(org.hamcrest.Matchers.containsString("email")));
    }

    @Test
    void registerWithDuplicateEmailReturns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("juan@raceflow.dev");
        req.setPassword("Str0ng!Pass");
        req.setName("Juan");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: juan@raceflow.dev"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already registered")));
    }

    @Test
    void registerWithMissingContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/auth/register").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
