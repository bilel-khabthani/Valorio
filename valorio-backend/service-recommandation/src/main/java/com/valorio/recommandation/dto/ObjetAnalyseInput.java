package com.valorio.recommandation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.List;

public record ObjetAnalyseInput(
        Long categorieId,
        String categorie,
        String intitule,
        String etat,
        @DecimalMin("0") Double ageAnnees,
        @DecimalMin("0") Double valeurEstimee,
        @DecimalMin("0") Double coutReparationEstime,
        String ville,
        Double latitude,
        Double longitude,
        List<String> materiaux,
        @DecimalMin("0") @DecimalMax("1") Double tauxRecyclabilite
) {
}
