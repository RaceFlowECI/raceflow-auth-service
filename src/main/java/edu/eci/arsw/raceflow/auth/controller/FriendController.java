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

/**
 * REST endpoints for the friendship lifecycle: search, request, accept,
 * reject, and list friends. All routes act on the authenticated caller,
 * resolved from the JWT subject.
 */
@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendshipService friendshipService;

    /**
     * @param friendshipService service implementing the friendship lifecycle
     */
    public FriendController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    /**
     * Lists the caller's accepted friends.
     *
     * @return the caller's friend list
     */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> myFriends() {
        return ResponseEntity.ok(friendshipService.listFriends(self()));
    }

    /**
     * Searches athletes by name or email, excluding the caller.
     *
     * @param query free-text search term
     * @return matching profiles (at most 10)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(friendshipService.searchUsers(query, self()));
    }

    /**
     * Lists friend requests addressed to the caller that are still pending.
     *
     * @return pending requests with the requester's name
     */
    @GetMapping("/requests")
    public ResponseEntity<List<PendingRequestResponse>> pending() {
        return ResponseEntity.ok(friendshipService.pendingRequests(self()));
    }

    /**
     * Sends a friend request from the caller to the given email.
     *
     * @param dto the target athlete's email
     * @return {@code 201 Created} on success
     */
    @PostMapping("/requests")
    public ResponseEntity<Void> sendRequest(@Valid @RequestBody FriendRequestDto dto) {
        friendshipService.sendRequest(self(), dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Accepts a pending friend request addressed to the caller.
     *
     * @param id the friendship request id
     * @return {@code 204 No Content} on success
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        friendshipService.accept(self(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rejects (deletes) a pending friend request addressed to the caller.
     *
     * @param id the friendship request id
     * @return {@code 204 No Content} on success
     */
    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        friendshipService.reject(self(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * @return the authenticated caller's email, from the JWT subject
     */
    private String self() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
