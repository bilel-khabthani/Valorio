package com.valorio.objets.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record Objet(
        Long id,
        String intitule,
        String description,
        Long categorieId,
        EtatObjet etat,
        Integer ageAnnees,
        Double valeurEstimee,
        String ville,
        Double latitude,
        Double longitude,
        List<String> materiaux,
        Long proprietaireId,
        StatutObjet statut,
        List<String> photos,
        OffsetDateTime dateDeclaration,
        OffsetDateTime dateMaj
) {
}
