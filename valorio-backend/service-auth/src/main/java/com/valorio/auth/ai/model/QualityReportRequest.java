package com.valorio.auth.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QualityReportRequest(
        @NotBlank(message = "Le code source est obligatoire")
        @Size(max = 50000, message = "Le code ne peut dépasser 50 000 caractères")
        String code,

        @NotBlank(message = "Le langage est obligatoire")
        String language,

        Boolean includeStaticAnalysis,
        Boolean includeAiReview
) {}
