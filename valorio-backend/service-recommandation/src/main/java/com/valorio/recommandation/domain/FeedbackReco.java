package com.valorio.recommandation.domain;

import java.time.OffsetDateTime;

public record FeedbackReco(
        Long id,
        Long recommandationId,
        boolean utile,
        String commentaire,
        OffsetDateTime enregistreLe
) {
}
