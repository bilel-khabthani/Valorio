package com.valorio.auth.domain;

import java.time.OffsetDateTime;

public record User(
        Long id,
        String email,
        String passwordHash,
        String nom,
        String prenom,
        Role role,
        Acteur acteur,
        boolean actif,
        boolean emailVerifie,
        String ville,
        Double latitude,
        Double longitude,
        Double scoreReputation,
        OffsetDateTime dateCreation,
        OffsetDateTime dateMaj
) {

    public static final double SCORE_PAR_DEFAUT = 5.0;

    public User(Long id, String email, String passwordHash, String nom, String prenom, Role role,
                boolean actif, boolean emailVerifie, OffsetDateTime dateCreation, OffsetDateTime dateMaj) {
        this(id, email, passwordHash, nom, prenom, role, acteurParDefaut(role), actif, emailVerifie,
                null, null, null, SCORE_PAR_DEFAUT, dateCreation, dateMaj);
    }

    public User(Long id, String email, String passwordHash, String nom, String prenom, Role role, Acteur acteur,
                boolean actif, boolean emailVerifie, OffsetDateTime dateCreation, OffsetDateTime dateMaj) {
        this(id, email, passwordHash, nom, prenom, role, acteur, actif, emailVerifie,
                null, null, null, SCORE_PAR_DEFAUT, dateCreation, dateMaj);
    }

    private static Acteur acteurParDefaut(Role role) {
        return role == Role.ADMIN ? Acteur.ADMIN : Acteur.UTILISATEUR;
    }

    public String nomComplet() {
        return (prenom + " " + nom).trim();
    }
}
