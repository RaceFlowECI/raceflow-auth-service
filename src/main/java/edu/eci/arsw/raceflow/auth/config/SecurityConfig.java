package edu.eci.arsw.raceflow.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion central de Spring Security: autenticacion JWT sin estado,
 * rutas publicas para registro/login/health, y CORS para los origenes
 * de frontend permitidos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * @param jwtAuthFilter filtro que autentica peticiones desde el encabezado JWT
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Define las reglas de seguridad HTTP: deshabilita CSRF (justificado abajo),
     * habilita CORS, fuerza sesiones sin estado, permite las rutas publicas de
     * auth y health, y exige autenticacion para todo lo demas.
     *
     * @param http el builder de seguridad provisto por Spring
     * @return la cadena de filtros configurada
     * @throws Exception si la configuracion de seguridad no se puede construir
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF relies on ambient browser credentials (cookies); this API is stateless and
                // authenticates via an explicit Authorization: Bearer header, which browsers never
                // attach automatically cross-origin, so CSRF does not apply here.
                .csrf(csrf -> csrf.disable()) // NOSONAR java:S4502 -- stateless JWT API, no session cookies
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Construye la configuracion de CORS aplicada a todas las rutas, permitiendo
     * los origenes locales de desarrollo y el frontend de produccion en Azure Static Web Apps.
     *
     * @return la fuente de configuracion de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://lively-rock-0066b1e0f.7.azurestaticapps.net"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
