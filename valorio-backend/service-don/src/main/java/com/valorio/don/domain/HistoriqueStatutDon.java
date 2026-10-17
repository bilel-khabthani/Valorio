package com.valorio.don.domain;

import java.time.OffsetDateTime;

public record HistoriqueStatutDon(
        StatutDemandeDon statut,
        OffsetDateTime date,
        String commentaire
) {
}
