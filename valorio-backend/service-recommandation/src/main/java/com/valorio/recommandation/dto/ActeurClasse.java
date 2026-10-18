package com.valorio.recommandation.dto;

import java.util.List;

public record ActeurClasse(
        Long candidatId,
        Double score,
        Double probabiliteAcceptation,
        Double delaiJoursEstime,
        Double distanceKm,
        List<String> raisons
) {
}
