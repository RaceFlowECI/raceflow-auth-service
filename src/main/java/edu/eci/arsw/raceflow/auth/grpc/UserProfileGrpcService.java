package edu.eci.arsw.raceflow.auth.grpc;

import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Internal gRPC service that lets other services (namely realtime-service)
 * resolve an athlete's authoritative name/sport by email, instead of
 * trusting whatever the client sent in a REST request body.
 */
@Component
public class UserProfileGrpcService extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserRepository userRepository;

    /**
     * @param userRepository source of truth for user profiles
     */
    public UserProfileGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Looks up a user by email and streams back their profile, or a
     * {@code found=false} response if no such user exists.
     *
     * @param request          the email to look up
     * @param responseObserver gRPC's callback channel for the response
     */
    @Override
    public void getProfile(ProfileRequest request, StreamObserver<ProfileResponse> responseObserver) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        ProfileResponse response = user
                .map(u -> ProfileResponse.newBuilder()
                        .setFound(true)
                        .setEmail(u.getEmail())
                        .setName(u.getName())
                        .setSport(u.getSport() == null ? "" : u.getSport())
                        .build())
                .orElseGet(() -> ProfileResponse.newBuilder()
                        .setFound(false)
                        .setEmail(request.getEmail())
                        .build());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
