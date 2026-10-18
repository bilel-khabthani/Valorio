package com.valorio.objets.domain;

public enum StatutObjet {
    DECLARE,
    DISPONIBLE,
    EN_REPARATION,
    REPARE,
    VENDU,
    DONNE,
    RECYCLE;

    public boolean peutTransitionnerVers(StatutObjet cible) {
        if (cible == null) {
            return false;
        }
        return switch (this) {
            case DECLARE -> cible == DISPONIBLE || cible == EN_REPARATION
                    || cible == VENDU || cible == DONNE || cible == RECYCLE;
            case DISPONIBLE -> cible == EN_REPARATION || cible == VENDU
                    || cible == DONNE || cible == RECYCLE;
            case EN_REPARATION -> cible == REPARE || cible == DISPONIBLE;
            case REPARE -> cible == DISPONIBLE || cible == VENDU || cible == DONNE;
            case VENDU, DONNE, RECYCLE -> false;
        };
    }
}
