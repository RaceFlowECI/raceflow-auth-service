package edu.eci.arsw.raceflow.auth.service;

import edu.eci.arsw.raceflow.auth.dto.PendingRequestResponse;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.entity.Friendship;
import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.exception.FriendshipException;
import edu.eci.arsw.raceflow.auth.repository.FriendshipRepository;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Implementa el ciclo de vida de amistades: buscar, solicitar, aceptar/rechazar, y listar. */
@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * @param friendshipRepository persistencia para filas de {@link Friendship}
     * @param userRepository       persistencia para filas de {@link User}
     */
    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    /**
     * @param query     subcadena comparada contra email o nombre
     * @param selfEmail el email del solicitante, excluido de los resultados
     * @return maximo 10 perfiles que coinciden
     */
    public List<UserProfileResponse> searchUsers(String query, String selfEmail) {
        return userRepository
                .findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query)
                .stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(selfEmail))
                .map(this::toProfile)
                .toList();
    }

    /**
     * Envia una solicitud de amistad. Rechaza auto-solicitarse, un destino
     * desconocido, o una solicitud duplicada en cualquier direccion (incluyendo una
     * amistad ya existente).
     *
     * @param selfEmail   el email del solicitante
     * @param targetEmail el email del destinatario
     * @throws FriendshipException en cualquiera de los casos invalidos anteriores
     */
    public void sendRequest(String selfEmail, String targetEmail) {
        if (selfEmail.equalsIgnoreCase(targetEmail)) {
            throw new FriendshipException("No puedes enviarte una solicitud a ti mismo");
        }
        if (userRepository.findByEmail(targetEmail).isEmpty()) {
            throw new FriendshipException("No existe un usuario con ese correo");
        }
        friendshipRepository.findBetween(selfEmail, targetEmail).ifPresent(f -> {
            throw new FriendshipException(f.getStatus() == Friendship.Status.ACCEPTED
                    ? "Ya son amigos" : "Ya hay una solicitud pendiente entre ustedes");
        });

        friendshipRepository.save(Friendship.builder()
                .requesterEmail(selfEmail)
                .addresseeEmail(targetEmail)
                .status(Friendship.Status.PENDING)
                .build());
    }

    /**
     * Acepta una solicitud pendiente. Solo el destinatario puede aceptarla.
     *
     * @param selfEmail el email del solicitante (debe ser el destinatario)
     * @param requestId el id de la solicitud de amistad
     * @throws FriendshipException if the request doesn't exist, isn't
     *         addressed to the caller, or was already answered
     */
    public void accept(String selfEmail, Long requestId) {
        Friendship f = pendingAddressedTo(selfEmail, requestId);
        f.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(f);
    }

    /**
     * Rechaza (elimina) una solicitud pendiente. Solo el destinatario puede rechazarla.
     *
     * @param selfEmail el email del solicitante (debe ser el destinatario)
     * @param requestId el id de la solicitud de amistad
     * @throws FriendshipException if the request doesn't exist, isn't
     *         addressed to the caller, or was already answered
     */
    public void reject(String selfEmail, Long requestId) {
        friendshipRepository.delete(pendingAddressedTo(selfEmail, requestId));
    }

    /**
     * @param selfEmail el email del solicitante
     * @return solicitudes pendientes dirigidas al solicitante, con los nombres de los solicitantes resueltos
     */
    public List<PendingRequestResponse> pendingRequests(String selfEmail) {
        return friendshipRepository
                .findByAddresseeEmailAndStatus(selfEmail, Friendship.Status.PENDING)
                .stream()
                .map(f -> PendingRequestResponse.builder()
                        .id(f.getId())
                        .fromEmail(f.getRequesterEmail())
                        .fromName(nameOf(f.getRequesterEmail()))
                        .build())
                .toList();
    }

    /**
     * @param selfEmail el email del solicitante
     * @return los amigos aceptados del solicitante (el otro lado de cada amistad)
     */
    public List<UserProfileResponse> listFriends(String selfEmail) {
        return friendshipRepository.findAcceptedInvolving(selfEmail).stream()
                .map(f -> f.getRequesterEmail().equalsIgnoreCase(selfEmail)
                        ? f.getAddresseeEmail() : f.getRequesterEmail())
                .map(email -> userRepository.findByEmail(email)
                        .map(this::toProfile)
                        .orElse(UserProfileResponse.builder().email(email).name(email).build()))
                .toList();
    }

    /**
     * Obtiene una solicitud y verifica que este pendiente y dirigida al solicitante.
     *
     * @param selfEmail destinatario esperado
     * @param requestId el id de la solicitud de amistad
     * @return la solicitud validada, aun pendiente
     * @throws FriendshipException si alguna validacion falla
     */
    private Friendship pendingAddressedTo(String selfEmail, Long requestId) {
        Friendship f = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new FriendshipException("La solicitud no existe"));
        if (!f.getAddresseeEmail().equalsIgnoreCase(selfEmail)) {
            throw new FriendshipException("La solicitud no está dirigida a ti");
        }
        if (f.getStatus() != Friendship.Status.PENDING) {
            throw new FriendshipException("La solicitud ya fue respondida");
        }
        return f;
    }

    private String nameOf(String email) {
        return userRepository.findByEmail(email).map(User::getName).orElse(email);
    }

    private UserProfileResponse toProfile(User u) {
        return UserProfileResponse.builder()
                .email(u.getEmail())
                .name(u.getName())
                .sport(u.getSport())
                .build();
    }
}
