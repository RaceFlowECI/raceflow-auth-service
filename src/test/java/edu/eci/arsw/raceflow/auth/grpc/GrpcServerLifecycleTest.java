package edu.eci.arsw.raceflow.auth.grpc;

import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;

class GrpcServerLifecycleTest {

    private UserProfileGrpcService realService() {
        return new UserProfileGrpcService(Mockito.mock(UserRepository.class));
    }

    @Test
    void startsAndStopsCleanlyOnAnEphemeralPort() {
        // Port 0 lets the OS pick a free ephemeral port, avoiding clashes with a real auth-service instance.
        GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(realService(), 0);

        assertThatCode(() -> {
            lifecycle.start();
            lifecycle.stop();
        }).doesNotThrowAnyException();
    }

    @Test
    void stopIsNoOpWhenServerWasNeverStarted() {
        GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(realService(), 0);

        assertThatCode(lifecycle::stop).doesNotThrowAnyException();
    }
}
