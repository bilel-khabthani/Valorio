package com.valorio.recommandation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @NotNull Long recommandationId,
        @NotNull Boolean utile,
        @Size(max = 500) String commentaire
) {
}
