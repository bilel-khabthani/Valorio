package com.valorio.recommandation.dto;

public record RaisonReco(
        String feature,
        String valeur,
        Double contribution,
        String libelle
) {
}
