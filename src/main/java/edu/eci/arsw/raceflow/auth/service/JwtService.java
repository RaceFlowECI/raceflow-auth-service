package edu.eci.arsw.raceflow.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Emite, analiza y valida JWTs firmados con HS256 para autenticacion. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * @param secret       secreto de firma HMAC (de {@code jwt.secret})
     * @param expirationMs vida util del token en milisegundos (de {@code jwt.expiration})
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Emite un JWT firmado con el email como subject y el nombre como un claim
     * personalizado (para que el frontend pueda mostrarlo sin una consulta extra).
     *
     * @param email el email del usuario, usado como subject del token
     * @param name  el nombre visible del usuario, almacenado como el claim {@code name}
     * @return el JWT compacto y firmado
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
     * @param token un JWT firmado
     * @return el subject del token (email)
     * @throws JwtException si el token esta mal formado o su firma es invalida
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
     * @param token un JWT a validar
     * @return {@code true} si la firma es valida y el token no ha expirado
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
