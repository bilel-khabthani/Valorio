package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.SourcePrediction;

public record ExplicationResponse(
        Long recommandationId,
        String explication,
        SourcePrediction source
) {
}
