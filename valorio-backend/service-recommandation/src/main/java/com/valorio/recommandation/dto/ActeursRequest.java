package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.TypeActeur;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ActeursRequest(
        @NotNull TypeActeur typeActeur,
        ObjetAnalyseInput objet,
        Double latitude,
        Double longitude,
        Double rayonKm,
        List<CandidatActeur> candidats
) {
}
