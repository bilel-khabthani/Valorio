package com.valorio.reparation.domain;

public class TransitionInterditeException extends IllegalStateException {

    public TransitionInterditeException(String message) {
        super(message);
    }
}
