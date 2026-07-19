package edu.eci.arsw.raceflow.auth.exception;

/** Thrown for any invalid friendship operation (self-request, duplicate, unauthorized answer, etc.). */
public class FriendshipException extends RuntimeException {
    public FriendshipException(String message) {
        super(message);
    }
}
