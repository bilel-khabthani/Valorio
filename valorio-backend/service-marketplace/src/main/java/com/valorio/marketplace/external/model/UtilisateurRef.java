package com.valorio.marketplace.external.model;

public record UtilisateurRef(
        Long id,
        String nom,
        String prenom,
        String email,
        String ville,
        Double latitude,
        Double longitude,
        Double scoreReputation
) {
}
