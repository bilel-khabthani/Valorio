package com.valorio.reparation.domain;

public record Reparateur(
        Long id,
        Long utilisateurId,
        String ville,
        boolean disponible,
        double noteMoyenne,
        int nbAvis,
        String competences,
        String categories
) {
}
