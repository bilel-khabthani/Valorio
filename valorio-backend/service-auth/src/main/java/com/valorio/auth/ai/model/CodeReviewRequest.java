package com.valorio.auth.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeReviewRequest(
        @NotBlank(message = "Le code à analyser est obligatoire")
        @Size(max = 50000, message = "Le code ne peut dépasser 50 000 caractères")
        String code,

        @NotBlank(message = "Le langage est obligatoire")
        String language,

        @Size(max = 10000, message = "Le contexte ne peut dépasser 10 000 caractères")
        String context
) {}
