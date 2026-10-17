package com.valorio.don.domain;

public enum StatutDemandeDon {
    DEMANDE,
    ACCEPTEE,
    REMISE_ORGANISEE,
    CONFIRMEE,
    REFUSEE;

    public boolean peutTransitionnerVers(StatutDemandeDon cible) {
        if (cible == null) {
            return false;
        }
        if (cible == REFUSEE) {
            return this == DEMANDE || this == ACCEPTEE;
        }
        if (this == REFUSEE) {
            return false;
        }
        return cible.ordinal() == this.ordinal() + 1;
    }
}
