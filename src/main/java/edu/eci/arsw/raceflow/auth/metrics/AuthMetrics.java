package edu.eci.arsw.raceflow.auth.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

/** Micrometer counters/gauges for auth-service, exposed at {@code /actuator/prometheus}. */
@Component
public class AuthMetrics {

    private final Counter registrations;
    private final Counter loginFailures;
    private final AtomicInteger activeTokens = new AtomicInteger(0);

    /**
     * @param registry the Micrometer registry to bind these meters to
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

    /** Increments the total registrations counter. */
    public void recordRegistration() { registrations.increment(); }
    /** Increments the total failed-login counter. */
    public void recordLoginFailure() { loginFailures.increment(); }
    /** Increments the active-tokens gauge. */
    public void incrementActiveTokens() { activeTokens.incrementAndGet(); }
    /** Decrements the active-tokens gauge. */
    public void decrementActiveTokens() { activeTokens.decrementAndGet(); }
}
