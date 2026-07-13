package edu.eci.arsw.raceflow.auth.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

/** Contadores/gauges de Micrometer para auth-service, expuestos en {@code /actuator/prometheus}. */
@Component
public class AuthMetrics {

    private final Counter registrations;
    private final Counter loginFailures;
    private final AtomicInteger activeTokens = new AtomicInteger(0);

    /**
     * @param registry el registro de Micrometer al que se enlazan estos meters
     */
    public AuthMetrics(MeterRegistry registry) {
        this.registrations = Counter.builder("raceflow.auth.registrations")
                .description("Total user registrations")
                .register(registry);

        this.loginFailures = Counter.builder("raceflow.auth.login.failures")
                .description("Total failed login attempts")
                .register(registry);

        Gauge.builder("raceflow.auth.active.tokens", activeTokens, AtomicInteger::get)
                .description("Currently active JWT tokens")
                .register(registry);
    }

    /** Incrementa el contador total de registros. */
    public void recordRegistration() { registrations.increment(); }
    /** Incrementa el contador total de logins fallidos. */
    public void recordLoginFailure() { loginFailures.increment(); }
    /** Incrementa el gauge de tokens activos. */
    public void incrementActiveTokens() { activeTokens.incrementAndGet(); }
    /** Decrementa el gauge de tokens activos. */
    public void decrementActiveTokens() { activeTokens.decrementAndGet(); }
}
