package com.valorio.recyclage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.valorio.recyclage.external.model.ObjetRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegleRecyclabiliteTest {

    private static ObjetRef objet(Long id, List<String> materiaux) {
        return new ObjetRef(id, 1L, "Objet " + id, "ENDOMMAGE", 3, 100.0, "Tunis", materiaux, 1L);
    }

    @Test
    void verdictOuiSiTousLesMateriauxSontValorisables() {
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(
                objet(1L, List.of("plastique", "aluminium", "verre")));
        assertThat(analyse.recyclable()).isEqualTo(VerdictRecyclabilite.OUI);
        assertThat(analyse.motifs()).contains("Tous les matériaux sont valorisables");
    }

    @Test
    void verdictPartielSiMelangeValorisableEtNonValorisable() {
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(objet(2L, List.of("plastique", "tissu")));
        assertThat(analyse.recyclable()).isEqualTo(VerdictRecyclabilite.PARTIEL);
        assertThat(analyse.motifs()).anyMatch(motif -> motif.contains("tissu"));
    }

    @Test
    void verdictNonSiAucunMateriauValorisable() {
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(objet(3L, List.of("tissu")));
        assertThat(analyse.recyclable()).isEqualTo(VerdictRecyclabilite.NON);
    }

    @Test
    void verdictNonSiAucunMateriau() {
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(objet(4L, List.of()));
        assertThat(analyse.recyclable()).isEqualTo(VerdictRecyclabilite.NON);
        assertThat(analyse.motifs()).contains("Aucun matériau identifié sur l'objet");
    }

    @Test
    void normaliseLaCasseEtLesDoublons() {
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(
                objet(5L, List.of("Plastique", " PLASTIQUE ", "ALUMINIUM")));
        assertThat(analyse.recyclable()).isEqualTo(VerdictRecyclabilite.OUI);
        assertThat(analyse.materiaux()).containsExactly("plastique", "aluminium");
    }
}
