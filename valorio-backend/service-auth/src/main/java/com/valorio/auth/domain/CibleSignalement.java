package com.valorio.auth.domain;

/** Type d'entité visée par un signalement (module 1, sous-module 1.3). */
public enum CibleSignalement {
    UTILISATEUR,
    OBJET,
    ANNONCE_DON,
    ANNONCE_MARKETPLACE,
    CENTRE,
    AUTRE;

    public static CibleSignalement depuis(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return AUTRE;
        }
        try {
            return valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AUTRE;
        }
    }
}
