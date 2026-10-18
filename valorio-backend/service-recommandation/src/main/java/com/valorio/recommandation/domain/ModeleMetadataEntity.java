package com.valorio.recommandation.domain;

import java.time.OffsetDateTime;

public record ModeleMetadataEntity(
        Long id,
        String nomModele,
        String version,
        boolean actif,
        String metriques,
        String topFeatures,
        OffsetDateTime majLe
) {
}
