package com.valorio.recommandation.dto;

public record CandidatActeur(
        Long id,
        String ville,
        Double latitude,
        Double longitude,
        Double noteMoyenne,
        Integer nbAvis,
        Boolean disponible,
        String competences,
        String categories
) {
}
