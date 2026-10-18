package com.valorio.recommandation.external.model;

import com.valorio.recommandation.domain.TypeActeur;
import java.util.List;

public record ActeurRef(
        Long id,
        Long utilisateurId,
        TypeActeur typeActeur,
        String nom,
        String ville,
        Double latitude,
        Double longitude,
        Double noteMoyenne,
        Integer nbAvis,
        Boolean disponible,
        String competences,
        String categories,
        List<String> materiauxAcceptes
) {
}
