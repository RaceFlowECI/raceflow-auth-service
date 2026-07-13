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

/** Implements the friendship lifecycle: search, request, accept/reject, and listing. */
@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * @param friendshipRepository persistence for {@link Friendship} rows
     * @param userRepository       persistence for {@link User} rows
     */
    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    /**
     * @param query     substring matched against email or name
     * @param selfEmail the caller's email, excluded from results
     * @return at most 10 matching profiles
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
     * Sends a friend request. Rejects requesting oneself, an unknown target,
     * or a duplicate request in either direction (including an existing
     * friendship).
     *
     * @param selfEmail   the requester's email
     * @param targetEmail the addressee's email
     * @throws FriendshipException on any of the invalid cases above
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
     * Accepts a pending request. Only the addressee may accept it.
     *
     * @param selfEmail the caller's email (must be the addressee)
     * @param requestId the friendship request id
     * @throws FriendshipException if the request doesn't exist, isn't
     *         addressed to the caller, or was already answered
     */
    public void accept(String selfEmail, Long requestId) {
        Friendship f = pendingAddressedTo(selfEmail, requestId);
        f.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(f);
    }

    /**
     * Rejects (deletes) a pending request. Only the addressee may reject it.
     *
     * @param selfEmail the caller's email (must be the addressee)
     * @param requestId the friendship request id
     * @throws FriendshipException if the request doesn't exist, isn't
     *         addressed to the caller, or was already answered
     */
    public void reject(String selfEmail, Long requestId) {
        friendshipRepository.delete(pendingAddressedTo(selfEmail, requestId));
    }

    /**
     * @param selfEmail the caller's email
     * @return pending requests addressed to the caller, with requester names resolved
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
     * @param selfEmail the caller's email
     * @return the caller's accepted friends (the other side of each friendship)
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
     * Fetches a request and verifies it is pending and addressed to the caller.
     *
     * @param selfEmail expected addressee
     * @param requestId the friendship request id
     * @return the validated, still-pending request
     * @throws FriendshipException if any validation fails
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
