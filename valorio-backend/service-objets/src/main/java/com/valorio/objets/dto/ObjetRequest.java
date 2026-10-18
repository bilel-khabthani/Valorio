package com.valorio.objets.dto;

import com.valorio.objets.domain.EtatObjet;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record ObjetRequest(
        @NotBlank(message = "L'intitulé est obligatoire")
        String intitule,
        String description,
        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId,
        @NotNull(message = "L'état est obligatoire")
        EtatObjet etat,
        @Min(value = 0, message = "L'âge ne peut pas être négatif")
        Integer ageAnnees,
        @PositiveOrZero(message = "La valeur estimée ne peut pas être négative")
        Double valeurEstimee,
        String ville,
        Double latitude,
        Double longitude,
        List<String> materiaux,
        @NotNull(message = "Le propriétaire est obligatoire")
        Long proprietaireId
) {
}
