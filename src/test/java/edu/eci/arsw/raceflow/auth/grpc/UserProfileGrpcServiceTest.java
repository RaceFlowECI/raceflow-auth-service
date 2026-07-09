package edu.eci.arsw.raceflow.auth.grpc;

import edu.eci.arsw.raceflow.auth.entity.User;
import edu.eci.arsw.raceflow.auth.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileGrpcServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StreamObserver<ProfileResponse> responseObserver;

    private UserProfileGrpcService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserProfileGrpcService(userRepository);
    }

    @Test
    void returnsFoundProfileForExistingUser() {
        User user = User.builder().email("juan@raceflow.dev").name("Juan").sport("ciclismo").build();
        when(userRepository.findByEmail("juan@raceflow.dev")).thenReturn(Optional.of(user));

        ProfileRequest request = ProfileRequest.newBuilder().setEmail("juan@raceflow.dev").build();
        service.getProfile(request, responseObserver);

        ArgumentCaptor<ProfileResponse> captor = ArgumentCaptor.forClass(ProfileResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ProfileResponse response = captor.getValue();
        assertThat(response.getFound()).isTrue();
        assertThat(response.getEmail()).isEqualTo("juan@raceflow.dev");
        assertThat(response.getName()).isEqualTo("Juan");
        assertThat(response.getSport()).isEqualTo("ciclismo");
    }

    @Test
    void returnsNotFoundForUnknownUser() {
        when(userRepository.findByEmail("ghost@raceflow.dev")).thenReturn(Optional.empty());

        ProfileRequest request = ProfileRequest.newBuilder().setEmail("ghost@raceflow.dev").build();
        service.getProfile(request, responseObserver);

        ArgumentCaptor<ProfileResponse> captor = ArgumentCaptor.forClass(ProfileResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        assertThat(captor.getValue().getFound()).isFalse();
        assertThat(captor.getValue().getEmail()).isEqualTo("ghost@raceflow.dev");
    }

    @Test
    void handlesNullSportGracefully() {
        User user = User.builder().email("ana@raceflow.dev").name("Ana").sport(null).build();
        when(userRepository.findByEmail("ana@raceflow.dev")).thenReturn(Optional.of(user));

        ProfileRequest request = ProfileRequest.newBuilder().setEmail("ana@raceflow.dev").build();
        service.getProfile(request, responseObserver);

        ArgumentCaptor<ProfileResponse> captor = ArgumentCaptor.forClass(ProfileResponse.class);
        verify(responseObserver).onNext(captor.capture());

        assertThat(captor.getValue().getSport()).isEmpty();
    }
}
