package edu.eci.arsw.raceflow.auth.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Starts the internal gRPC server manually (ServerBuilder.forPort(...).addService(...).build())
 * on its own port, separate from the REST port. Deliberately NOT using server.awaitTermination()
 * synchronously -- that would block Spring Boot's own startup thread and the embedded Tomcat
 * server would never come up. start()/shutdown() are called from Spring's lifecycle callbacks
 * instead.
 */
@Component
public class GrpcServerLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final UserProfileGrpcService userProfileGrpcService;
    private final int port;
    private Server server;

    public GrpcServerLifecycle(UserProfileGrpcService userProfileGrpcService,
                                @Value("${grpc.server.port:9090}") int port) {
        this.userProfileGrpcService = userProfileGrpcService;
        this.port = port;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(userProfileGrpcService)
                .build()
                .start();
        log.info("gRPC server started on {}", port);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }
}
