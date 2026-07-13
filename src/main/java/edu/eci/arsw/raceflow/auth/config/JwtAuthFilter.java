package edu.eci.arsw.raceflow.auth.config;

import edu.eci.arsw.raceflow.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de servlet que autentica cada peticion leyendo y validando
 * el JWT del encabezado {@code Authorization: Bearer}. Si es exitoso,
 * llena el contexto de Spring Security con el subject del token (email) para
 * que los controladores puedan resolver el usuario autenticado.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * @param jwtService servicio usado para validar tokens y extraer claims
     */
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extrae y valida el token bearer, si esta presente, y autentica
     * la peticion correspondientemente antes de continuar la cadena de filtros.
     * Las peticiones sin un token valido simplemente continuan sin autenticar;
     * las reglas de seguridad posteriores deciden si eso es aceptable para la ruta.
     *
     * @param request     la peticion HTTP entrante
     * @param response    la respuesta HTTP saliente
     * @param filterChain la cadena de filtros restante a invocar
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isTokenValid(token)) {
                String email = jwtService.extractEmail(token);
                var authToken = new UsernamePasswordAuthenticationToken(email, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
