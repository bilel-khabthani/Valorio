package com.valorio.recyclage.external.model;

import java.util.List;

public record ObjetRef(
        Long id,
        Long categorieId,
        String intitule,
        String etat,
        Integer ageAnnees,
        Double valeurEstimee,
        String ville,
        List<String> materiaux,
        Long proprietaireId
) {
}
