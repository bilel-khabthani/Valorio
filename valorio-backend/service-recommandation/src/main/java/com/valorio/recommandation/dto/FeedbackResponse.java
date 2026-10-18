package com.valorio.recommandation.dto;

import java.time.OffsetDateTime;

public record FeedbackResponse(
        Long id,
        Long recommandationId,
        boolean utile,
        OffsetDateTime enregistreLe
) {
}
