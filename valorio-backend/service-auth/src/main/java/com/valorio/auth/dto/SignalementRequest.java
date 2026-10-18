package com.valorio.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignalementRequest(
        Long signaleurId,
        String cibleType,
        Long cibleId,
        String motif,
        String description
) {
    public SignalementRequest {
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Le motif du signalement est obligatoire");
        }
    }
}
