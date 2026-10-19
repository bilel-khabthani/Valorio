package com.valorio.reparation.ai.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Résultat d'un diagnostic assisté par IA : catégorie et gravité de la panne,
 * explication, causes probables, estimation de devis et réparateurs recommandés.
 *
 * @param source origine de l'analyse ({@code gemini} ou {@code heuristique})
 */
public record DiagnosticResponse(
        String categoriePanne,
        String gravite,
        String diagnostic,
        List<String> causesProbables,
        DevisEstime devisEstime,
        List<ReparateurRecommande> reparateursRecommandes,
        double confiance,
        String source,
        List<String> avertissements
) {

    public record DevisEstime(
            BigDecimal montantMin,
            BigDecimal montantMax,
            int delaiJours,
            String devise
    ) {
    }

    public record ReparateurRecommande(
            Long reparateurId,
            double score,
            String ville
    ) {
    }
}
