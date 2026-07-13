package edu.eci.arsw.raceflow.auth.exception;

/** Se lanza para cualquier operacion de amistad invalida (auto-solicitud, duplicada, respuesta no autorizada, etc.). */
public class FriendshipException extends RuntimeException {
    public FriendshipException(String message) {
        super(message);
    }
}
