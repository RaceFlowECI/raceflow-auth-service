package edu.eci.arsw.raceflow.auth.controller;

import edu.eci.arsw.raceflow.auth.dto.FriendRequestDto;
import edu.eci.arsw.raceflow.auth.dto.PendingRequestResponse;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.service.FriendshipService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FriendControllerTest {

    @Mock
    private FriendshipService friendshipService;

    private FriendController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new FriendController(friendshipService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("juan@raceflow.dev", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void myFriendsDelegatesWithAuthenticatedEmail() {
        List<UserProfileResponse> friends = List.of(UserProfileResponse.builder().email("ana@raceflow.dev").build());
        when(friendshipService.listFriends("juan@raceflow.dev")).thenReturn(friends);

        assertThat(controller.myFriends().getBody()).isEqualTo(friends);
    }

    @Test
    void searchDelegatesQueryAndSelf() {
        when(friendshipService.searchUsers("ana", "juan@raceflow.dev")).thenReturn(List.of());

        assertThat(controller.search("ana").getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(friendshipService).searchUsers("ana", "juan@raceflow.dev");
    }

    @Test
    void pendingDelegates() {
        when(friendshipService.pendingRequests("juan@raceflow.dev"))
                .thenReturn(List.of(PendingRequestResponse.builder().id(1L).build()));

        assertThat(controller.pending().getBody()).hasSize(1);
    }

    @Test
    void sendRequestReturns201() {
        FriendRequestDto dto = new FriendRequestDto("ana@raceflow.dev");

        assertThat(controller.sendRequest(dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(friendshipService).sendRequest("juan@raceflow.dev", "ana@raceflow.dev");
    }

    @Test
    void acceptReturns204() {
        assertThat(controller.accept(7L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(friendshipService).accept("juan@raceflow.dev", 7L);
    }

    @Test
    void rejectReturns204() {
        assertThat(controller.reject(7L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(friendshipService).reject("juan@raceflow.dev", 7L);
    }
}
