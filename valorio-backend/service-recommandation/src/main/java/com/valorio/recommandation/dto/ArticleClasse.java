package com.valorio.recommandation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.List;

public record ArticleClasse(
        Long articleId,
        String titre,
        @DecimalMin("0") @DecimalMax("1") Double similarite,
        @DecimalMin("0") @DecimalMax("1") Double scoreDeal,
        Double prix,
        Double prixRecommande,
        String equitePrix,
        List<String> raisons
) {
}
