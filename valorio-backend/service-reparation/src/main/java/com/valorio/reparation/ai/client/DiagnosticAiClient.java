package com.valorio.reparation.ai.client;

import com.valorio.reparation.ai.model.DiagnosticRequest;
import com.valorio.reparation.ai.model.DiagnosticResponse;

/**
 * Abstraction du moteur d'analyse de panne. Deux implémentations :
 * {@link GeminiDiagnosticClient} (modèle Gemini via Google AI Studio) et
 * {@link HeuristicDiagnosticClient} (moteur local déterministe, hors-ligne).
 */
public interface DiagnosticAiClient {

    DiagnosticResponse diagnostiquer(DiagnosticRequest request);

    /** Identifiant technique du fournisseur (ex: {@code gemini}, {@code heuristique}). */
    String nom();
}
