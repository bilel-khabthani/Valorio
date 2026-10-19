package com.valorio.reparation.ai.security;

/** Levée lorsque l'utilisateur est authentifié mais n'a pas le rôle requis (HTTP 403). */
public class AccesInterditException extends RuntimeException {

    public AccesInterditException(String message) {
        super(message);
    }
}
