package com.valorio.don.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record DemandeDon(
        Long id,
        Long annonceId,
        Long beneficiaireId,
        String message,
        StatutDemandeDon statut,
        OffsetDateTime dateDemande,
        OffsetDateTime dateConfirmation,
        List<HistoriqueStatutDon> historique
) {
}
