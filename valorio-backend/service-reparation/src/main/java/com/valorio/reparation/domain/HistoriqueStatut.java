package com.valorio.reparation.domain;

import java.time.OffsetDateTime;

public record HistoriqueStatut(
        StatutDemande statut,
        OffsetDateTime date,
        String commentaire
) {
}
