package com.valorio.reparation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatutDemandeTest {

    @Test
    void autoriseUniquementLEtapeSuivante() {
        assertThat(StatutDemande.DEMANDE.peutTransitionnerVers(StatutDemande.ACCEPTEE)).isTrue();
        assertThat(StatutDemande.ACCEPTEE.peutTransitionnerVers(StatutDemande.DIAGNOSTIC)).isTrue();
        assertThat(StatutDemande.DIAGNOSTIC.peutTransitionnerVers(StatutDemande.EN_REPARATION)).isTrue();
        assertThat(StatutDemande.EN_REPARATION.peutTransitionnerVers(StatutDemande.TERMINEE)).isTrue();
        assertThat(StatutDemande.TERMINEE.peutTransitionnerVers(StatutDemande.LIVREE)).isTrue();
    }

    @Test
    void interditLeSautDEtapes() {
        assertThat(StatutDemande.DEMANDE.peutTransitionnerVers(StatutDemande.TERMINEE)).isFalse();
        assertThat(StatutDemande.DEMANDE.peutTransitionnerVers(StatutDemande.EN_REPARATION)).isFalse();
        assertThat(StatutDemande.LIVREE.peutTransitionnerVers(StatutDemande.DEMANDE)).isFalse();
        assertThat(StatutDemande.EN_REPARATION.peutTransitionnerVers(StatutDemande.EN_REPARATION)).isFalse();
    }

    @Test
    void refuseUneCibleNulle() {
        assertThat(StatutDemande.DEMANDE.peutTransitionnerVers(null)).isFalse();
    }

    @Test
    void planificationInterventionUniquementApresAcceptation() {
        assertThat(StatutDemande.DEMANDE.autorisePlanificationIntervention()).isFalse();
        assertThat(StatutDemande.ACCEPTEE.autorisePlanificationIntervention()).isTrue();
        assertThat(StatutDemande.DIAGNOSTIC.autorisePlanificationIntervention()).isTrue();
        assertThat(StatutDemande.EN_REPARATION.autorisePlanificationIntervention()).isTrue();
        assertThat(StatutDemande.TERMINEE.autorisePlanificationIntervention()).isTrue();
        assertThat(StatutDemande.LIVREE.autorisePlanificationIntervention()).isFalse();
    }
}
