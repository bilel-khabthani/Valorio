package com.valorio.recyclage.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record DemandeCollecte(
        Long id,
        Long objetId,
        Long utilisateurId,
        Long centreId,
        StatutCollecte statut,
        OffsetDateTime dateDemande,
        OffsetDateTime datePrevue,
        OffsetDateTime dateReception,
        List<HistoriqueCollecte> historique
) {
}
