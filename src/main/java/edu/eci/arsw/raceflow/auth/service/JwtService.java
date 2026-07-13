package edu.eci.arsw.raceflow.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Issues, parses, and validates HS256-signed JWTs for authentication. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * @param secret       HMAC signing secret (from {@code jwt.secret})
     * @param expirationMs token lifetime in milliseconds (from {@code jwt.expiration})
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Issues a signed JWT with the email as subject and the name as a custom
     * claim (so the frontend can display it without an extra lookup).
     *
     * @param email the user's email, used as the token subject
     * @param name  the user's display name, stored as the {@code name} claim
     * @return the compact, signed JWT
     */
    public String generateToken(String email, String name) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(email)
                .claim("name", name)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * @param token a signed JWT
     * @return the token's subject (email)
     * @throws JwtException if the token is malformed or its signature is invalid
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * @param token a JWT to validate
     * @return {@code true} if the signature is valid and the token isn't expired
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
