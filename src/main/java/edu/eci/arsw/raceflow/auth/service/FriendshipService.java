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

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public List<UserProfileResponse> searchUsers(String query, String selfEmail) {
        return userRepository
                .findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query)
                .stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(selfEmail))
                .map(this::toProfile)
                .toList();
    }

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

    public void accept(String selfEmail, Long requestId) {
        Friendship f = pendingAddressedTo(selfEmail, requestId);
        f.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(f);
    }

    public void reject(String selfEmail, Long requestId) {
        friendshipRepository.delete(pendingAddressedTo(selfEmail, requestId));
    }

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

    public List<UserProfileResponse> listFriends(String selfEmail) {
        return friendshipRepository.findAcceptedInvolving(selfEmail).stream()
                .map(f -> f.getRequesterEmail().equalsIgnoreCase(selfEmail)
                        ? f.getAddresseeEmail() : f.getRequesterEmail())
                .map(email -> userRepository.findByEmail(email)
                        .map(this::toProfile)
                        .orElse(UserProfileResponse.builder().email(email).name(email).build()))
                .toList();
    }

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
