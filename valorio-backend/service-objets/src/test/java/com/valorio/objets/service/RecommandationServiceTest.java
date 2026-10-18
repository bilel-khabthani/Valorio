package com.valorio.objets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.Recommandation;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecommandationServiceTest {

    private final RecommandationService service = new RecommandationService();

    @Test
    void objetNeufEstRecommandeALaRevente() {
        Recommandation r = service.recommander(objet(EtatObjet.NEUF, 800.0, List.of("plastique")), null);

        assertEquals(RecommandationService.REVENDRE, r.action());
        assertNull(r.coutReparationEstime());
    }

    @Test
    void objetBonEtatDeHauteValeurEstRecommandeALaRevente() {
        Recommandation r = service.recommander(objet(EtatObjet.BON, 300.0, List.of("acier")), null);

        assertEquals(RecommandationService.REVENDRE, r.action());
    }

    @Test
    void objetBonEtatDeFaibleValeurEstRecommandeAuDon() {
        Recommandation r = service.recommander(objet(EtatObjet.BON, 20.0, List.of("bois")), null);

        assertEquals(RecommandationService.DONNER, r.action());
    }

    @Test
    void objetEndommageRentableEstRecommandeALaReparation() {
        Recommandation r = service.recommander(
                objet(EtatObjet.ENDOMMAGE, 500.0, List.of("aluminium")),
                diagnostic(List.of("ecran fissure")));

        assertEquals(RecommandationService.REPARER, r.action());
        assertEquals(150.0, r.coutReparationEstime());
        assertEquals(500.0, r.valeurApresReparationEstimee());
        assertEquals(350.0, r.economieEstimee());
    }

    @Test
    void objetEndommageSansValeurAvecMateriauxValorisablesEstRecycle() {
        Recommandation r = service.recommander(
                objet(EtatObjet.ENDOMMAGE, 50.0, List.of("plastique", "cuivre")),
                diagnostic(List.of("moteur bloque", "chassis plie", "interrupteur casse")));

        assertEquals(RecommandationService.RECYCLER, r.action());
    }

    @Test
    void objetHorsServiceAvecMateriauxValorisablesEstRecycle() {
        Recommandation r = service.recommander(
                objet(EtatObjet.HORS_SERVICE, 10.0, List.of("acier", "caoutchouc")),
                diagnostic(List.of("roue voilee")));

        assertEquals(RecommandationService.RECYCLER, r.action());
        assertEquals("L'objet n'est ni réparable de façon rentable ni revendable ; ses matériaux valorisables (acier, caoutchouc) peuvent être recyclés.",
                r.justification());
    }

    @Test
    void casseLesMateriauxValorisablesEstIgnore() {
        Recommandation r = service.recommander(
                objet(EtatObjet.HORS_SERVICE, 10.0, List.of("CUIVRE", "acier", "cassee")),
                diagnostic(List.of("fissure")));

        assertEquals(RecommandationService.RECYCLER, r.action());
        assertEquals("L'objet n'est ni réparable de façon rentable ni revendable ; ses matériaux valorisables (cuivre, acier) peuvent être recyclés.",
                r.justification());
    }

    private static Objet objet(EtatObjet etat, Double valeur, List<String> materiaux) {
        return new Objet(1L, "Objet", null, 1L, etat, 2, valeur, "Tunis", null, null,
                materiaux, 1L, com.valorio.objets.domain.StatutObjet.DECLARE, List.of(),
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    private static DiagnosticObjet diagnostic(List<String> problemes) {
        return new DiagnosticObjet(1L, 1L, null, problemes, null, null, OffsetDateTime.now());
    }
}
