package edu.eci.arsw.raceflow.auth.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthMetrics {

    private final Counter registrations;
    private final Counter loginFailures;
    private final AtomicInteger activeTokens = new AtomicInteger(0);

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

    public void recordRegistration() { registrations.increment(); }
    public void recordLoginFailure() { loginFailures.increment(); }
    public void incrementActiveTokens() { activeTokens.incrementAndGet(); }
    public void decrementActiveTokens() { activeTokens.decrementAndGet(); }
}
