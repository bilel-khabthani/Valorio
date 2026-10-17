package com.valorio.auth.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestGenerationRequest(
        @NotBlank(message = "Le code source à tester est obligatoire")
        @Size(max = 50000, message = "Le code ne peut dépasser 50 000 caractères")
        String code,

        @NotBlank(message = "Le langage est obligatoire")
        String language,

        String framework,
        String testType,

        @Size(max = 10000, message = "Le contexte ne peut dépasser 10 000 caractères")
        String context
) {}
