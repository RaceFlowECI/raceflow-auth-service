package edu.eci.arsw.raceflow.auth.exception;

/** Se lanza cuando se intenta registrar con un email que ya esta en uso. */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
