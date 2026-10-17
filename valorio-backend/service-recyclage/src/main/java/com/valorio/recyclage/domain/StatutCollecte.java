package com.valorio.recyclage.domain;

public enum StatutCollecte {
    DEMANDE,
    PLANIFIEE,
    EN_COURS,
    RECUE,
    ANNULEE;

    public boolean peutTransitionnerVers(StatutCollecte cible) {
        if (cible == null) {
            return false;
        }
        return switch (this) {
            case DEMANDE -> cible == PLANIFIEE || cible == ANNULEE;
            case PLANIFIEE -> cible == EN_COURS || cible == ANNULEE;
            case EN_COURS -> cible == RECUE || cible == ANNULEE;
            case RECUE, ANNULEE -> false;
        };
    }
}
