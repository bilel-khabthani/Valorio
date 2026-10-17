package com.valorio.don.domain;

public enum StatutAnnonceDon {
    PUBLIEE,
    RESERVEE,
    REMISE,
    ANNULEE;

    public boolean peutTransitionnerVers(StatutAnnonceDon cible) {
        if (cible == null) {
            return false;
        }
        return switch (this) {
            case PUBLIEE -> cible == RESERVEE || cible == ANNULEE;
            case RESERVEE -> cible == REMISE || cible == PUBLIEE || cible == ANNULEE;
            case REMISE, ANNULEE -> false;
        };
    }
}
