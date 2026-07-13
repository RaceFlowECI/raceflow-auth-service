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
 * Endpoints REST para el ciclo de vida de amistades: buscar, solicitar, aceptar,
 * rechazar, y listar amigos. Todas las rutas actuan sobre el solicitante autenticado,
 * resuelto a partir del subject del JWT.
 */
@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendshipService friendshipService;

    /**
     * @param friendshipService servicio que implementa el ciclo de vida de amistades
     */
    public FriendController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    /**
     * Lista los amigos aceptados del solicitante.
     *
     * @return la lista de amigos del solicitante
     */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> myFriends() {
        return ResponseEntity.ok(friendshipService.listFriends(self()));
    }

    /**
     * Busca atletas por nombre o email, excluyendo al solicitante.
     *
     * @param query termino de busqueda libre
     * @return perfiles que coinciden (maximo 10)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(friendshipService.searchUsers(query, self()));
    }

    /**
     * Lista las solicitudes de amistad dirigidas al solicitante que siguen pendientes.
     *
     * @return solicitudes pendientes con el nombre del solicitante original
     */
    @GetMapping("/requests")
    public ResponseEntity<List<PendingRequestResponse>> pending() {
        return ResponseEntity.ok(friendshipService.pendingRequests(self()));
    }

    /**
     * Envia una solicitud de amistad del solicitante al email dado.
     *
     * @param dto el email del atleta destino
     * @return {@code 201 Created} si tiene exito
     */
    @PostMapping("/requests")
    public ResponseEntity<Void> sendRequest(@Valid @RequestBody FriendRequestDto dto) {
        friendshipService.sendRequest(self(), dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Acepta una solicitud de amistad pendiente dirigida al solicitante.
     *
     * @param id el id de la solicitud de amistad
     * @return {@code 204 No Content} si tiene exito
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        friendshipService.accept(self(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechaza (elimina) una solicitud de amistad pendiente dirigida al solicitante.
     *
     * @param id el id de la solicitud de amistad
     * @return {@code 204 No Content} si tiene exito
     */
    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        friendshipService.reject(self(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * @return el email del solicitante autenticado, desde el subject del JWT
     */
    private String self() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
