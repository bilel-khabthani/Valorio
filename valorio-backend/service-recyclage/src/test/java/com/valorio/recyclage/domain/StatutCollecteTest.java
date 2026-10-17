package com.valorio.recyclage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatutCollecteTest {

    @Test
    void autoriseLaSuiteNominale() {
        assertThat(StatutCollecte.DEMANDE.peutTransitionnerVers(StatutCollecte.PLANIFIEE)).isTrue();
        assertThat(StatutCollecte.PLANIFIEE.peutTransitionnerVers(StatutCollecte.EN_COURS)).isTrue();
        assertThat(StatutCollecte.EN_COURS.peutTransitionnerVers(StatutCollecte.RECUE)).isTrue();
    }

    @Test
    void autoriseLAnnulationAvantReception() {
        assertThat(StatutCollecte.DEMANDE.peutTransitionnerVers(StatutCollecte.ANNULEE)).isTrue();
        assertThat(StatutCollecte.PLANIFIEE.peutTransitionnerVers(StatutCollecte.ANNULEE)).isTrue();
        assertThat(StatutCollecte.EN_COURS.peutTransitionnerVers(StatutCollecte.ANNULEE)).isTrue();
        assertThat(StatutCollecte.RECUE.peutTransitionnerVers(StatutCollecte.ANNULEE)).isFalse();
    }

    @Test
    void interditDeSauterDesEtapes() {
        assertThat(StatutCollecte.DEMANDE.peutTransitionnerVers(StatutCollecte.RECUE)).isFalse();
        assertThat(StatutCollecte.DEMANDE.peutTransitionnerVers(StatutCollecte.EN_COURS)).isFalse();
        assertThat(StatutCollecte.RECUE.peutTransitionnerVers(StatutCollecte.EN_COURS)).isFalse();
    }
}
