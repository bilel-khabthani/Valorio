package com.valorio.objets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.StatutObjet;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyseDiagnosticTest {

    @Test
    void analyseSansPanneEtSansProbleme() {
        String analyse = AnalyseDiagnostic.analyser(objet(EtatObjet.BON, 120.0, 5), null, List.of());

        assertEquals("Objet BON, âge 5 ans, valeur estimée 120.0 DT — 0 problème(s) constaté(s).", analyse);
    }

    @Test
    void analyseAvecPanneEtProblemes() {
        String analyse = AnalyseDiagnostic.analyser(objet(EtatObjet.ENDOMMAGE, 650.0, 3),
                "Ecran fendu", List.of("ecran fissure", "charniere desseree", "ecran fissure"));

        assertTrue(analyse.contains("2 problème(s) constaté(s)"));
        assertTrue(analyse.contains("ecran fissure, charniere desseree"));
        assertTrue(analyse.contains("Panne déclarée : Ecran fendu"));
    }

    @Test
    void diagnosticBonEtatSansProbleme() {
        assertEquals("Aucun dysfonctionnement majeur détecté : un entretien courant suffit.",
                AnalyseDiagnostic.diagnosticPreliminaire(EtatObjet.BON, List.of()));
    }

    @Test
    void diagnosticEndommageAvecDeuxProblemesEstReparable() {
        String diagnostic = AnalyseDiagnostic.diagnosticPreliminaire(EtatObjet.ENDOMMAGE,
                List.of("ecran fissure", "charniere"));

        assertTrue(diagnostic.startsWith("Réparation envisageable"));
    }

    @Test
    void diagnosticEndommageAvecPlusieursProblemesEstComplexe() {
        String diagnostic = AnalyseDiagnostic.diagnosticPreliminaire(EtatObjet.ENDOMMAGE,
                List.of("a", "b", "c"));

        assertTrue(diagnostic.startsWith("Réparation complexe"));
    }

    @Test
    void diagnosticHorsServiceOrienteVersLeRecyclage() {
        String diagnostic = AnalyseDiagnostic.diagnosticPreliminaire(EtatObjet.HORS_SERVICE,
                List.of("moteur bloque"));

        assertTrue(diagnostic.contains("recyclabilité"));
    }

    private static Objet objet(EtatObjet etat, Double valeur, int age) {
        return new Objet(1L, "Objet", null, 1L, etat, age, valeur, "Tunis", null, null,
                List.of(), 1L, StatutObjet.DECLARE, List.of(), OffsetDateTime.now(), OffsetDateTime.now());
    }
}
