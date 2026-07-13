package edu.eci.arsw.raceflow.auth.service;

import edu.eci.arsw.raceflow.auth.dto.PendingRequestResponse;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.entity.Friendship;
import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.exception.FriendshipException;
import edu.eci.arsw.raceflow.auth.repository.FriendshipRepository;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    private FriendshipService service;

    private final User ana = User.builder().id(2L).email("ana@raceflow.dev").name("Ana").sport("running").build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FriendshipService(friendshipRepository, userRepository);
    }

    @Test
    void searchExcludesSelfAndMapsProfiles() {
        User self = User.builder().email("juan@raceflow.dev").name("Juan").build();
        when(userRepository.findTop10ByEmailContainingIgnoreCaseOrNameContainingIgnoreCase("an", "an"))
                .thenReturn(List.of(self, ana));

        List<UserProfileResponse> result = service.searchUsers("an", "juan@raceflow.dev");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ana@raceflow.dev");
        assertThat(result.get(0).getSport()).isEqualTo("running");
    }

    @Test
    void sendRequestPersistsPendingFriendship() {
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(ana));
        when(friendshipRepository.findBetween(anyString(), anyString())).thenReturn(Optional.empty());

        service.sendRequest("juan@raceflow.dev", "ana@raceflow.dev");

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(captor.capture());
        assertThat(captor.getValue().getRequesterEmail()).isEqualTo("juan@raceflow.dev");
        assertThat(captor.getValue().getAddresseeEmail()).isEqualTo("ana@raceflow.dev");
        assertThat(captor.getValue().getStatus()).isEqualTo(Friendship.Status.PENDING);
    }

    @Test
    void sendRequestToSelfIsRejected() {
        assertThatThrownBy(() -> service.sendRequest("juan@raceflow.dev", "juan@raceflow.dev"))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("ti mismo");
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestToUnknownUserIsRejected() {
        when(userRepository.findByEmail("nadie@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendRequest("juan@raceflow.dev", "nadie@x.com"))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void sendRequestWhenAlreadyFriendsIsRejected() {
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(ana));
        when(friendshipRepository.findBetween("juan@raceflow.dev", "ana@raceflow.dev"))
                .thenReturn(Optional.of(Friendship.builder().status(Friendship.Status.ACCEPTED).build()));

        assertThatThrownBy(() -> service.sendRequest("juan@raceflow.dev", "ana@raceflow.dev"))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("Ya son amigos");
    }

    @Test
    void sendRequestWhenPendingExistsIsRejected() {
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(ana));
        when(friendshipRepository.findBetween("juan@raceflow.dev", "ana@raceflow.dev"))
                .thenReturn(Optional.of(Friendship.builder().status(Friendship.Status.PENDING).build()));

        assertThatThrownBy(() -> service.sendRequest("juan@raceflow.dev", "ana@raceflow.dev"))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("pendiente");
    }

    @Test
    void acceptMarksRequestAccepted() {
        Friendship pending = Friendship.builder()
                .id(7L).requesterEmail("ana@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.PENDING).build();
        when(friendshipRepository.findById(7L)).thenReturn(Optional.of(pending));

        service.accept("juan@raceflow.dev", 7L);

        assertThat(pending.getStatus()).isEqualTo(Friendship.Status.ACCEPTED);
        verify(friendshipRepository).save(pending);
    }

    @Test
    void acceptByWrongAddresseeIsRejected() {
        Friendship pending = Friendship.builder()
                .id(7L).requesterEmail("ana@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.PENDING).build();
        when(friendshipRepository.findById(7L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.accept("leo@raceflow.dev", 7L))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("dirigida");
    }

    @Test
    void acceptAlreadyAnsweredIsRejected() {
        Friendship accepted = Friendship.builder()
                .id(7L).requesterEmail("ana@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.ACCEPTED).build();
        when(friendshipRepository.findById(7L)).thenReturn(Optional.of(accepted));

        assertThatThrownBy(() -> service.accept("juan@raceflow.dev", 7L))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("respondida");
    }

    @Test
    void acceptMissingRequestIsRejected() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept("juan@raceflow.dev", 99L))
                .isInstanceOf(FriendshipException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void rejectDeletesPendingRequest() {
        Friendship pending = Friendship.builder()
                .id(7L).requesterEmail("ana@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.PENDING).build();
        when(friendshipRepository.findById(7L)).thenReturn(Optional.of(pending));

        service.reject("juan@raceflow.dev", 7L);

        verify(friendshipRepository).delete(pending);
    }

    @Test
    void pendingRequestsResolveRequesterNames() {
        Friendship pending = Friendship.builder()
                .id(7L).requesterEmail("ana@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.PENDING).build();
        when(friendshipRepository.findByAddresseeEmailAndStatus("juan@raceflow.dev", Friendship.Status.PENDING))
                .thenReturn(List.of(pending));
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(ana));

        List<PendingRequestResponse> result = service.pendingRequests("juan@raceflow.dev");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromName()).isEqualTo("Ana");
        assertThat(result.get(0).getId()).isEqualTo(7L);
    }

    @Test
    void listFriendsReturnsTheOtherSideOfEachFriendship() {
        Friendship iRequested = Friendship.builder()
                .requesterEmail("juan@raceflow.dev").addresseeEmail("ana@raceflow.dev")
                .status(Friendship.Status.ACCEPTED).build();
        Friendship theyRequested = Friendship.builder()
                .requesterEmail("leo@raceflow.dev").addresseeEmail("juan@raceflow.dev")
                .status(Friendship.Status.ACCEPTED).build();
        when(friendshipRepository.findAcceptedInvolving("juan@raceflow.dev"))
                .thenReturn(List.of(iRequested, theyRequested));
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(ana));
        when(userRepository.findByEmail("leo@raceflow.dev")).thenReturn(Optional.empty());

        List<UserProfileResponse> friends = service.listFriends("juan@raceflow.dev");

        assertThat(friends).extracting(UserProfileResponse::getEmail)
                .containsExactly("ana@raceflow.dev", "leo@raceflow.dev");
        assertThat(friends.get(1).getName()).isEqualTo("leo@raceflow.dev");
    }
}
