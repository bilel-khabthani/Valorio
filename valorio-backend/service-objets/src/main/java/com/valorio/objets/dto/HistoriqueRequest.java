package com.valorio.objets.dto;

import com.valorio.objets.domain.StatutObjet;
import com.valorio.objets.domain.TypeEvenement;
import jakarta.validation.constraints.NotNull;

public record HistoriqueRequest(
        @NotNull(message = "Le type d'événement est obligatoire")
        TypeEvenement type,
        String libelle,
        Long acteurId,
        StatutObjet statutCible
) {
}
