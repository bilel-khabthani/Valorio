package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.SourcePrediction;
import java.time.OffsetDateTime;

public record HistoriqueReco(
        Long id,
        Long objetId,
        String typeReco,
        String chargeUtile,
        String resultat,
        SourcePrediction source,
        OffsetDateTime creeLe
) {
}
