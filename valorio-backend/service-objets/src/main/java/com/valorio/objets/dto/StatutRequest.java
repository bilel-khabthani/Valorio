package com.valorio.objets.dto;

import com.valorio.objets.domain.StatutObjet;
import jakarta.validation.constraints.NotNull;

public record StatutRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutObjet statut,
        String commentaire
) {
}
