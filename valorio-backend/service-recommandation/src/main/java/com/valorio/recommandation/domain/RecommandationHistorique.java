package com.valorio.recommandation.domain;

import java.time.OffsetDateTime;

public record RecommandationHistorique(
        Long id,
        Long objetId,
        String typeReco,
        String chargeUtile,
        String resultat,
        String source,
        String modeleVersion,
        OffsetDateTime creeLe
) {
}
