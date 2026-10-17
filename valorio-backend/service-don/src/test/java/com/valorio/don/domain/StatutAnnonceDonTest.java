package com.valorio.don.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatutAnnonceDonTest {

    @Test
    void uneAnnoncePublieePeutEtreReserveeOuAnnulee() {
        assertThat(StatutAnnonceDon.PUBLIEE.peutTransitionnerVers(StatutAnnonceDon.RESERVEE)).isTrue();
        assertThat(StatutAnnonceDon.PUBLIEE.peutTransitionnerVers(StatutAnnonceDon.ANNULEE)).isTrue();
    }

    @Test
    void uneAnnonceReserveePeutEtreRemisePublieeOuAnnulee() {
        assertThat(StatutAnnonceDon.RESERVEE.peutTransitionnerVers(StatutAnnonceDon.REMISE)).isTrue();
        assertThat(StatutAnnonceDon.RESERVEE.peutTransitionnerVers(StatutAnnonceDon.PUBLIEE)).isTrue();
        assertThat(StatutAnnonceDon.RESERVEE.peutTransitionnerVers(StatutAnnonceDon.ANNULEE)).isTrue();
    }

    @Test
    void uneAnnonceTermineeNePeutPlusChanger() {
        assertThat(StatutAnnonceDon.REMISE.peutTransitionnerVers(StatutAnnonceDon.ANNULEE)).isFalse();
        assertThat(StatutAnnonceDon.ANNULEE.peutTransitionnerVers(StatutAnnonceDon.PUBLIEE)).isFalse();
        assertThat(StatutAnnonceDon.ANNULEE.peutTransitionnerVers(StatutAnnonceDon.RESERVEE)).isFalse();
    }

    @Test
    void refuseUneCibleNulle() {
        assertThat(StatutAnnonceDon.PUBLIEE.peutTransitionnerVers(null)).isFalse();
    }
}
