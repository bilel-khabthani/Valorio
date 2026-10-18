package com.valorio.objets.domain;

import java.time.OffsetDateTime;

public record EvenementHistorique(
        Long id,
        Long objetId,
        TypeEvenement type,
        String libelle,
        Long acteurId,
        StatutObjet statutCible,
        OffsetDateTime date
) {
}
