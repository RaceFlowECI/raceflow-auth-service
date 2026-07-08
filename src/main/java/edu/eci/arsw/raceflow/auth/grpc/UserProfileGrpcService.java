package edu.eci.arsw.raceflow.auth.grpc;

import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserProfileGrpcService extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserRepository userRepository;

    public UserProfileGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
