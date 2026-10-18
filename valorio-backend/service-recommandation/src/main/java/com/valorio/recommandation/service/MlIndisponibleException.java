package com.valorio.recommandation.service;

public class MlIndisponibleException extends RuntimeException {

    public MlIndisponibleException(String message) {
        super(message);
    }

    public MlIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
