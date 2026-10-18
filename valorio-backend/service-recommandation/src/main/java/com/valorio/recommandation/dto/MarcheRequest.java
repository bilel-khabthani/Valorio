package com.valorio.recommandation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MarcheRequest(
        @NotNull ArticleMarche requete,
        List<ArticleMarche> candidats,
        @Min(1) @Max(20) Integer topK
) {
}
