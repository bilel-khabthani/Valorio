package com.valorio.auth.domain;

import java.time.OffsetDateTime;

public record User(
        Long id,
        String email,
        String passwordHash,
        String nom,
        String prenom,
        Role role,
        boolean actif,
        boolean emailVerifie,
        OffsetDateTime dateCreation,
        OffsetDateTime dateMaj
) {
    public String nomComplet() {
        return (prenom + " " + nom).trim();
    }
}
