package com.valorio.reparation.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Devis(
        Long id,
        Long demandeId,
        BigDecimal montant,
        Integer delaiJours,
        String commentaire,
        StatutDevis statut,
        OffsetDateTime dateCreation
) {
}
