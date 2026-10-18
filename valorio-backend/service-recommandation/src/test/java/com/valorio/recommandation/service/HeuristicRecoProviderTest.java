package com.valorio.recommandation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.valorio.recommandation.domain.RouteValorisation;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.dto.ActeurClasse;
import com.valorio.recommandation.dto.ArticleClasse;
import com.valorio.recommandation.dto.ArticleMarche;
import com.valorio.recommandation.dto.CandidatActeur;
import com.valorio.recommandation.dto.ObjetAnalyseInput;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class HeuristicRecoProviderTest {

    private final HeuristicRecoProvider provider = new HeuristicRecoProvider();

    private static ObjetAnalyseInput objet(String etat, double age, double valeur, double cout,
                                           List<String> materiaux) {
        return new ObjetAnalyseInput(1L, "Electronique", "Objet de test", etat, age, valeur, cout,
                "Tunis", 36.8065, 10.1815, materiaux, null);
    }

    @Test
    void endommageFaibleCoutRatioRecommandeReparation() {
        RecoValorisationResponse reco = provider.recommanderValorisation(
                objet("ENDOMMAGE", 3, 500, 100, List.of("plastique", "verre")));

        assertThat(reco.routeRecommandee()).isEqualTo(RouteValorisation.REPARATION);
        assertThat(reco.probabilite()).isNotNull().isBetween(0.62, 0.78);
        assertThat(reco.source()).isEqualTo(SourcePrediction.HEURISTIQUE);
        assertThat(reco.raisons()).hasSize(3);
        assertThat(reco.raisons()).extracting(r -> r.feature())
                .containsExactly("coutRatio", "etat", "tauxRecyclabilite");
        assertThat(reco.alternatives()).hasSize(3);
        assertThat(reco.alternatives()).extracting(a -> a.route())
                .doesNotContain(RouteValorisation.REPARATION);
        assertThat(reco.alternatives()).isSortedAccordingTo(
                (a, b) -> Double.compare(b.probabilite(), a.probabilite()));
        assertThat(reco.alternatives().get(0).probabilite())
                .isLessThan(reco.probabilite());
    }

    @Test
    void horsServiceTauxRecyclabiliteEleveRecommandeRecyclage() {
        RecoValorisationResponse reco = provider.recommanderValorisation(
                objet("HORS_SERVICE", 8, 15, 0, List.of("plastique", "aluminium")));

        assertThat(reco.routeRecommandee()).isEqualTo(RouteValorisation.RECYCLAGE);
        assertThat(reco.probabilite()).isBetween(0.62, 0.78);
        assertThat(reco.raisons().get(2).libelle()).contains("Taux de recyclabilité");
    }

    @Test
    void neufRecentValeurEleveeRecommandeRevente() {
        RecoValorisationResponse reco = provider.recommanderValorisation(
                objet("NEUF", 1, 300, 0, List.of("plastique")));

        assertThat(reco.routeRecommandee()).isEqualTo(RouteValorisation.REVENTE);
        assertThat(reco.probabilite()).isBetween(0.62, 0.78);
    }

    @Test
    void bonFaibleValeurRecommandeDon() {
        RecoValorisationResponse reco = provider.recommanderValorisation(
                objet("BON", 6, 30, 0, List.of("tissu")));

        assertThat(reco.routeRecommandee()).isEqualTo(RouteValorisation.DON);
        assertThat(reco.probabilite()).isBetween(0.62, 0.78);
    }

    @Test
    void endommageCoutEleveNonRecyclableRecommandeDon() {
        RecoValorisationResponse reco = provider.recommanderValorisation(
                objet("ENDOMMAGE", 5, 100, 100, List.of("tissu")));

        assertThat(reco.routeRecommandee()).isEqualTo(RouteValorisation.DON);
    }

    @Test
    void tauxRecyclabilitePartageLesMateriauxValorisables() {
        assertThat(HeuristicRecoProvider.tauxRecyclabilite(List.of("plastique", "tissu")))
                .isEqualTo(0.5);
        assertThat(HeuristicRecoProvider.tauxRecyclabilite(List.of("acier", "aluminium")))
                .isEqualTo(1.0);
        assertThat(HeuristicRecoProvider.tauxRecyclabilite(List.of("tissu")))
                .isEqualTo(0.0);
        assertThat(HeuristicRecoProvider.tauxRecyclabilite(List.of()))
                .isEqualTo(0.0);
    }

    @Test
    void classementActeursTrieEtDisponibleLemporte() {
        CandidatActeur indisponibleNotee = new CandidatActeur(1L, "Tunis", 36.8065, 10.1815,
                4.8, 30, false, "electronique,ecran", "1");
        CandidatActeur disponible = new CandidatActeur(2L, "Tunis", 36.8065, 10.1815,
                4.0, 9, true, "electronique,ecran", "1");
        ObjetAnalyseInput demande = new ObjetAnalyseInput(1L, "Electronique", "Ecran casse",
                "ENDOMMAGE", 3.0, 400.0, 100.0, "Tunis", 36.8065, 10.1815,
                List.of("plastique"), null);

        List<ActeurClasse> classement = provider.classementActeurs(
                List.of(indisponibleNotee, disponible), demande, 36.8065, 10.1815);

        assertThat(classement).hasSize(2);
        assertThat(classement.get(0).candidatId()).isEqualTo(2L);
        assertThat(classement.get(0).score()).isGreaterThanOrEqualTo(classement.get(1).score());
        assertThat(classement).isSortedAccordingTo(
                (a, b) -> Double.compare(b.score(), a.score()));
        assertThat(classement.get(0).raisons()).contains("Catégorie compatible avec la demande");
        assertThat(classement.get(0).raisons()).contains("Acteur disponible");
        assertThat(classement.get(1).raisons()).contains("Acteur indisponible");
        assertThat(classement.get(0).delaiJoursEstime()).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void classementMarcheClasseEquitePrix() {
        ArticleMarche requete = new ArticleMarche(null, "Ordinateur portable", "Electronique",
                "BON", 300.0, null);
        ArticleMarche bonMarche = new ArticleMarche(1L, "Ordinateur portable reconditionne",
                "Electronique", "BON", 100.0,
                "Ordinateur portable i5 reconditionne, ecran HD, SSD rapide.");
        ArticleMarche cher = new ArticleMarche(2L, "Ordinateur portable gaming RTX",
                "Electronique", "BON", 1000.0,
                "Ordinateur portable gaming RTX pour le travail et les jeux.");

        List<ArticleClasse> classes = provider.classementMarche(requete, List.of(cher, bonMarche));
        Map<Long, ArticleClasse> parId = classes.stream()
                .collect(Collectors.toMap(ArticleClasse::articleId, a -> a));

        assertThat(classes).hasSize(2);
        assertThat(parId.get(1L).equitePrix()).isEqualTo("EXCELLENT_DEAL");
        assertThat(parId.get(1L).prixRecommande()).isEqualTo(450.0);
        assertThat(parId.get(2L).equitePrix()).isEqualTo("OVERPRICED");
        assertThat(parId.get(2L).prixRecommande()).isEqualTo(450.0);
        assertThat(parId.get(1L).scoreDeal()).isBetween(0.0, 1.0);
        assertThat(classes.get(0).articleId()).isEqualTo(1L);
        assertThat(classes.get(0).similarite()).isGreaterThanOrEqualTo(classes.get(1).similarite());
    }
}
