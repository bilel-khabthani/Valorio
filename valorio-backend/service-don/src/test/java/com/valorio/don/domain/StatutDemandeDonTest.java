package com.valorio.don.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatutDemandeDonTest {

    @Test
    void autoriseLaSuiteNominale() {
        assertThat(StatutDemandeDon.DEMANDE.peutTransitionnerVers(StatutDemandeDon.ACCEPTEE)).isTrue();
        assertThat(StatutDemandeDon.ACCEPTEE.peutTransitionnerVers(StatutDemandeDon.REMISE_ORGANISEE)).isTrue();
        assertThat(StatutDemandeDon.REMISE_ORGANISEE.peutTransitionnerVers(StatutDemandeDon.CONFIRMEE)).isTrue();
    }

    @Test
    void autoriseLeRefusAvantConfirmation() {
        assertThat(StatutDemandeDon.DEMANDE.peutTransitionnerVers(StatutDemandeDon.REFUSEE)).isTrue();
        assertThat(StatutDemandeDon.ACCEPTEE.peutTransitionnerVers(StatutDemandeDon.REFUSEE)).isTrue();
        assertThat(StatutDemandeDon.CONFIRMEE.peutTransitionnerVers(StatutDemandeDon.REFUSEE)).isFalse();
    }

    @Test
    void interditDeSauterDesEtapes() {
        assertThat(StatutDemandeDon.DEMANDE.peutTransitionnerVers(StatutDemandeDon.CONFIRMEE)).isFalse();
        assertThat(StatutDemandeDon.DEMANDE.peutTransitionnerVers(StatutDemandeDon.REMISE_ORGANISEE)).isFalse();
    }
}
