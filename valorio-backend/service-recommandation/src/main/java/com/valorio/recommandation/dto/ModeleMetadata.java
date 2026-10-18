package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.SourcePrediction;
import java.util.List;
import java.util.Map;

public record ModeleMetadata(
        boolean actif,
        SourcePrediction source,
        String versionValorisation,
        String versionActeurs,
        String versionMarche,
        Map<String, Double> metriques,
        List<String> topFeatures
) {
}
