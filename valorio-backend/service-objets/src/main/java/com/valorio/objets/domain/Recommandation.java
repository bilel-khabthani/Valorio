package com.valorio.objets.domain;

public record Recommandation(
        String action,
        String justification,
        Double coutReparationEstime,
        Double valeurApresReparationEstimee,
        Double economieEstimee
) {
}
