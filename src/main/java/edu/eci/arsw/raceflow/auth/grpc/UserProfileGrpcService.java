package edu.eci.arsw.raceflow.auth.grpc;

import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Servicio gRPC interno que permite a otros servicios (concretamente realtime-service)
 * resolver el nombre/deporte autoritativo de un atleta por email, en vez de
 * confiar en lo que el cliente haya enviado en el cuerpo de una peticion REST.
 */
@Component
public class UserProfileGrpcService extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserRepository userRepository;

    /**
     * @param userRepository fuente de verdad para los perfiles de usuario
     */
    public UserProfileGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Busca un usuario por email y envia de vuelta su perfil, o una
     * respuesta {@code found=false} si no existe tal usuario.
     *
     * @param request          el email a buscar
     * @param responseObserver canal de callback de gRPC para la respuesta
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
