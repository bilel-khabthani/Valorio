package com.valorio.recyclage.domain;

import java.time.OffsetDateTime;

public record HistoriqueCollecte(
        StatutCollecte statut,
        OffsetDateTime date,
        String commentaire
) {
}
