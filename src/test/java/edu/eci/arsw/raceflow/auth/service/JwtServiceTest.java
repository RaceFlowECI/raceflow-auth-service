package edu.eci.arsw.raceflow.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "raceflow-dev-secret-key-for-local-dev-only-32chars";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86_400_000L);
    }

    @Test
    void generateTokenIsValidAndCarriesTheSubject() {
        String token = jwtService.generateToken("juan@raceflow.dev", "Juan");

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("juan@raceflow.dev");
    }

    @Test
    void generateTokenCarriesTheNameClaim() {
        String token = jwtService.generateToken("juan@raceflow.dev", "Juan Pérez");

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String name = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("name", String.class);

        assertThat(name).isEqualTo("Juan Pérez");
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        JwtService shortLived = new JwtService(SECRET, -1_000L);
        String token = shortLived.generateToken("juan@raceflow.dev", "Juan");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForTokenSignedWithDifferentSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor("a-completely-different-secret-key-32ch".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("juan@raceflow.dev")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
