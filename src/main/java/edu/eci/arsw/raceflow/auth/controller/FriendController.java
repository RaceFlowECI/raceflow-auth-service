package edu.eci.arsw.raceflow.auth.controller;

import edu.eci.arsw.raceflow.auth.dto.FriendRequestDto;
import edu.eci.arsw.raceflow.auth.dto.PendingRequestResponse;
import edu.eci.arsw.raceflow.auth.dto.UserProfileResponse;
import edu.eci.arsw.raceflow.auth.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendshipService friendshipService;

    public FriendController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> myFriends() {
        return ResponseEntity.ok(friendshipService.listFriends(self()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(friendshipService.searchUsers(query, self()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<PendingRequestResponse>> pending() {
        return ResponseEntity.ok(friendshipService.pendingRequests(self()));
    }

    @PostMapping("/requests")
    public ResponseEntity<Void> sendRequest(@Valid @RequestBody FriendRequestDto dto) {
        friendshipService.sendRequest(self(), dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        friendshipService.accept(self(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        friendshipService.reject(self(), id);
        return ResponseEntity.noContent().build();
    }

    private String self() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
