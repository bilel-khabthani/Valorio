package com.valorio.reparation.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record DemandeReparation(
        Long id,
        Long objetId,
        Long utilisateurId,
        Long reparateurId,
        String descriptionPanne,
        StatutDemande statut,
        OffsetDateTime dateIntervention,
        OffsetDateTime dateCreation,
        OffsetDateTime dateMaj,
        List<HistoriqueStatut> historiqueStatuts
) {
}
