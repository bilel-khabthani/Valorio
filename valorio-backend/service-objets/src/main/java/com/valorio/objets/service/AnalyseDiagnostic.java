package com.valorio.objets.service;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.Objet;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyse des caractéristiques de l'objet (module 2.2) : produit l'analyse
 * et le diagnostic préliminaire à partir de l'état déclaré et des problèmes constatés.
 */
public final class AnalyseDiagnostic {

    private AnalyseDiagnostic() {
    }

    public static String analyser(Objet objet, String panneDeclaree, List<String> problemes) {
        List<String> constates = normaliser(problemes);
        StringBuilder analyse = new StringBuilder();
        analyse.append("Objet ").append(objet.etat());
        if (objet.ageAnnees() != null) {
            analyse.append(", âge ").append(objet.ageAnnees()).append(objet.ageAnnees() > 1 ? " ans" : " an");
        }
        if (objet.valeurEstimee() != null) {
            analyse.append(", valeur estimée ").append(objet.valeurEstimee()).append(" DT");
        }
        analyse.append(" — ").append(constates.size()).append(" problème(s) constaté(s)");
        if (!constates.isEmpty()) {
            analyse.append(" : ").append(String.join(", ", constates));
        }
        analyse.append('.');
        if (panneDeclaree != null && !panneDeclaree.isBlank()) {
            analyse.append(" Panne déclarée : ").append(panneDeclaree.trim()).append('.');
        }
        return analyse.toString();
    }

    public static String diagnosticPreliminaire(EtatObjet etat, List<String> problemes) {
        List<String> constates = normaliser(problemes);
        if (constates.isEmpty() && (etat == EtatObjet.NEUF || etat == EtatObjet.BON)) {
            return "Aucun dysfonctionnement majeur détecté : un entretien courant suffit.";
        }
        return switch (etat) {
            case NEUF -> "Objet neuf avec " + constates.size()
                    + " anomalie(s) mineure(s) : vérifier le ou les composants signalés avant remise en service.";
            case BON -> "Objet en bon état général ; les problèmes constatés ("
                    + String.join(", ", constates)
                    + ") sont localisés et réparables à faible coût.";
            case ENDOMMAGE -> constates.size() <= 2
                    ? "Réparation envisageable : les problèmes constatés (" + String.join(", ", constates)
                            + ") sont localisés et ne compromettent pas la structure de l'objet."
                    : "Réparation complexe : " + constates.size()
                            + " composants concernés, un devis professionnel est recommandé avant toute décision.";
            case HORS_SERVICE -> "Objet hors service : usure avancée détectée"
                    + (constates.isEmpty() ? "" : " (" + String.join(", ", constates) + ")")
                    + ". Évaluer la recyclabilité des matériaux et la rentabilité d'une réparation.";
        };
    }

    private static List<String> normaliser(List<String> problemes) {
        if (problemes == null) {
            return List.of();
        }
        List<String> propres = new ArrayList<>();
        for (String probleme : problemes) {
            if (probleme != null && !probleme.isBlank()) {
                String valeur = probleme.trim();
                if (!propres.contains(valeur)) {
                    propres.add(valeur);
                }
            }
        }
        return List.copyOf(propres);
    }
}
