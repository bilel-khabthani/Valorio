package com.valorio.auth.dto;

import com.valorio.auth.domain.StatutSignalement;
import jakarta.validation.constraints.NotNull;

public record TraitementSignalementRequest(
        Long traitePar,
        @jakarta.validation.constraints.NotNull StatutSignalement statut,
        String note
) {
}
