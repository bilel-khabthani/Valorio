package com.valorio.don.domain;

import java.time.OffsetDateTime;

public record AnnonceDon(
        Long id,
        Long objetId,
        Long donneurId,
        String titre,
        String description,
        String conditions,
        String ville,
        Double latitude,
        Double longitude,
        StatutAnnonceDon statut,
        OffsetDateTime datePublication
) {
}
