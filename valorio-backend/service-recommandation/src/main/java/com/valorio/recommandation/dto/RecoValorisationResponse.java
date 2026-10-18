package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.RouteValorisation;
import com.valorio.recommandation.domain.SourcePrediction;
import java.util.List;

public record RecoValorisationResponse(
        Long id,
        RouteValorisation routeRecommandee,
        Double probabilite,
        List<AlternativeReco> alternatives,
        List<RaisonReco> raisons,
        Double confidenceScore,
        SourcePrediction source,
        String modeleVersion
) {
}
