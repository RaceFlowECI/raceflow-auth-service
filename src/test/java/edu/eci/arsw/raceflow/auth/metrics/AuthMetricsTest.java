package edu.eci.arsw.raceflow.auth.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMetricsTest {

    private MeterRegistry registry;
    private AuthMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new AuthMetrics(registry);
    }

    @Test
    void recordRegistrationIncrementsCounter() {
        metrics.recordRegistration();
        metrics.recordRegistration();

        assertThat(registry.get("raceflow.auth.registrations").counter().count())
                .isEqualTo(2.0);
    }

    @Test
    void recordLoginFailureIncrementsCounter() {
        metrics.recordLoginFailure();

        assertThat(registry.get("raceflow.auth.login.failures").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    void activeTokensGaugeTracksIncrementAndDecrement() {
        metrics.incrementActiveTokens();
        metrics.incrementActiveTokens();
        metrics.decrementActiveTokens();

        assertThat(registry.get("raceflow.auth.active.tokens").gauge().value())
                .isEqualTo(1.0);
    }
}
