package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.SourcePrediction;
import java.util.List;

public record RecoMarcheResponse(
        List<ArticleClasse> articles,
        SourcePrediction source,
        String modeleVersion
) {
}
