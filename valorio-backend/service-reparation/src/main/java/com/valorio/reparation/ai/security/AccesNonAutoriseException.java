package com.valorio.reparation.ai.security;

/** Levée lorsqu'aucun jeton d'authentification valide n'est fourni (HTTP 401). */
public class AccesNonAutoriseException extends RuntimeException {

    public AccesNonAutoriseException(String message) {
        super(message);
    }
}
