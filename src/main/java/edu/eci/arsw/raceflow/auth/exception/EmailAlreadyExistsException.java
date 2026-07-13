package edu.eci.arsw.raceflow.auth.exception;

/** Thrown when registration is attempted with an email that is already taken. */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
