package com.valorio.auth.domain;

/**
 * Les 5 acteurs du CDC Valorio (Atelier 2, section A) :
 * Utilisateur, Réparateur, Acheteur, Organisme de recyclage, Administrateur.
 */
public enum Acteur {
    UTILISATEUR,
    REPARATEUR,
    ACHETEUR,
    ORGANISME,
    ADMIN;

    /** L'acteur ADMIN est reserve aux comptes d'administration (jamais auto-attribue). */
    public static Acteur depuis(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return UTILISATEUR;
        }
        try {
            Acteur acteur = valueOf(valeur.trim().toUpperCase());
            return acteur == ADMIN ? UTILISATEUR : acteur;
        } catch (IllegalArgumentException e) {
            return UTILISATEUR;
        }
    }
}
