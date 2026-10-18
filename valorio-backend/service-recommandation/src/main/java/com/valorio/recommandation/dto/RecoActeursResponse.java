package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.domain.TypeActeur;
import java.util.List;

public record RecoActeursResponse(
        TypeActeur typeActeur,
        List<ActeurClasse> classement,
        SourcePrediction source,
        String modeleVersion
) {
}
