package com.valorio.objets.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatutObjetTest {

    @Test
    void declarePeutAllerVersLesStatutsOperationnels() {
        assertTrue(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.DISPONIBLE));
        assertTrue(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.EN_REPARATION));
        assertTrue(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.VENDU));
        assertTrue(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.DONNE));
        assertTrue(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.RECYCLE));
        assertFalse(StatutObjet.DECLARE.peutTransitionnerVers(StatutObjet.REPARE));
    }

    @Test
    void enReparationNePeutQueReparerOuRemettreDisponible() {
        assertTrue(StatutObjet.EN_REPARATION.peutTransitionnerVers(StatutObjet.REPARE));
        assertTrue(StatutObjet.EN_REPARATION.peutTransitionnerVers(StatutObjet.DISPONIBLE));
        assertFalse(StatutObjet.EN_REPARATION.peutTransitionnerVers(StatutObjet.VENDU));
        assertFalse(StatutObjet.EN_REPARATION.peutTransitionnerVers(StatutObjet.DONNE));
        assertFalse(StatutObjet.EN_REPARATION.peutTransitionnerVers(StatutObjet.RECYCLE));
    }

    @Test
    void statutsTerminauxNePermettentAucuneTransition() {
        for (StatutObjet terminal : new StatutObjet[]{StatutObjet.VENDU, StatutObjet.DONNE, StatutObjet.RECYCLE}) {
            for (StatutObjet cible : StatutObjet.values()) {
                assertFalse(terminal.peutTransitionnerVers(cible),
                        terminal + " ne doit pas transitionner vers " + cible);
            }
        }
    }

    @Test
    void cibleNullRefusee() {
        assertFalse(StatutObjet.DECLARE.peutTransitionnerVers(null));
    }
}
