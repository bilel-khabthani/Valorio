package com.valorio.reparation.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Demande d'analyse adressée à l'assistant IA de diagnostic.
 * Seule la description de la panne est obligatoire ; les autres champs servent
 * à enrichir le contexte (objet, état, âge, ville).
 */
public record DiagnosticRequest(
        Long objetId,
        @NotBlank(message = "la description de la panne est obligatoire")
        @jakarta.validation.constraints.Size(max = 2000)
        String descriptionPanne,
        String intitule,
        String etat,
        Integer ageAnnees,
        String ville
) {
}
