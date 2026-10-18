package com.valorio.auth.domain;

import java.time.OffsetDateTime;

public record Signalement(
        Long id,
        Long signaleurId,
        CibleSignalement cibleType,
        Long cibleId,
        String motif,
        String description,
        StatutSignalement statut,
        OffsetDateTime dateCreation,
        OffsetDateTime dateTraitement,
        Long traitePar,
        String noteTraitement
) {
}
