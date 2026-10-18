package com.valorio.recommandation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recommandation.domain.ModeleMetadataEntity;
import com.valorio.recommandation.domain.RecommandationHistorique;
import com.valorio.recommandation.domain.RouteValorisation;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.domain.TypeActeur;
import com.valorio.recommandation.dto.ActeursRequest;
import com.valorio.recommandation.dto.ArticleMarche;
import com.valorio.recommandation.dto.FeedbackRequest;
import com.valorio.recommandation.dto.FeedbackResponse;
import com.valorio.recommandation.dto.HistoriqueReco;
import com.valorio.recommandation.dto.MarcheRequest;
import com.valorio.recommandation.dto.ModeleMetadata;
import com.valorio.recommandation.dto.ValorisationRequest;
import com.valorio.recommandation.dto.RecoActeursResponse;
import com.valorio.recommandation.dto.RecoMarcheResponse;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import com.valorio.recommandation.external.ActeurPort;
import com.valorio.recommandation.external.CategoriePort;
import com.valorio.recommandation.external.MarchePort;
import com.valorio.recommandation.external.MarketplaceMarcheClient;
import com.valorio.recommandation.external.ObjetPort;
import com.valorio.recommandation.external.UtilisateurPort;
import com.valorio.recommandation.external.model.ActeurRef;
import com.valorio.recommandation.external.model.ArticleMarcheRef;
import com.valorio.recommandation.external.model.CategorieRef;
import com.valorio.recommandation.external.model.ObjetRef;
import com.valorio.recommandation.repo.RecommandationRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommandationServiceTest {

    @Mock private RecommandationRepository repository;
    @Mock private ObjetPort objets;
    @Mock private UtilisateurPort utilisateurs;
    @Mock private CategoriePort categories;
    @Mock private ActeurPort acteurs;
    @Mock private MarchePort marche;
    @Mock private MarketplaceMarcheClient marketplace;
    @Mock private MlClient ml;
    @Mock private ExplicationService explication;
    @Spy private ObjectMapper mapper = new ObjectMapper();
    @Spy private HeuristicRecoProvider heuristique = new HeuristicRecoProvider();

    @InjectMocks
    private RecommandationService service;

    private void objetExistant(long id) {
        given(objets.findById(id)).willReturn(Optional.of(new ObjetRef(
                id, 1L, "Ordinateur portable", "BON", 3, 400.0, "Tunis",
                List.of("plastique"), 10L)));
        given(categories.findById(1L)).willReturn(Optional.of(new CategorieRef(1L, "Electronique")));
    }

    @Test
    void utiliseLaPredictionMlQuandElleEstDisponible() {
        objetExistant(1L);
        given(ml.predictValorisation(any())).willReturn(new RecoValorisationResponse(
                null, RouteValorisation.REVENTE, 0.77, List.of(), List.of(),
                0.9, SourcePrediction.ML, "valorisation-gbm-2.0.0"));
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(42L);

        RecoValorisationResponse reponse =
                service.recommanderValorisation(new ValorisationRequest(1L, null));

        assertThat(reponse.id()).isEqualTo(42L);
        assertThat(reponse.routeRecommandee()).isEqualTo(RouteValorisation.REVENTE);
        assertThat(reponse.source()).isEqualTo(SourcePrediction.ML);
        assertThat(reponse.modeleVersion()).isEqualTo("valorisation-gbm-2.0.0");
        verify(repository).insertHistorique(eq(1L), eq("VALORISATION"), anyString(),
                anyString(), eq("ML"), eq("valorisation-gbm-2.0.0"));
    }

    @Test
    void repliqueHeuristiqueQuandLeMlEstIndisponible() {
        objetExistant(1L);
        willThrow(new MlIndisponibleException("ML injoignable"))
                .given(ml).predictValorisation(any());
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(7L);

        RecoValorisationResponse reponse =
                service.recommanderValorisation(new ValorisationRequest(1L, null));

        assertThat(reponse.id()).isEqualTo(7L);
        assertThat(reponse.routeRecommandee()).isNotNull();
        assertThat(reponse.source()).isEqualTo(SourcePrediction.HEURISTIQUE);
        assertThat(reponse.modeleVersion()).isEqualTo(HeuristicRecoProvider.VERSION_HEURISTIQUE);
        verify(repository).insertHistorique(eq(1L), eq("VALORISATION"), anyString(),
                anyString(), eq("HEURISTIQUE"), eq(HeuristicRecoProvider.VERSION_HEURISTIQUE));
    }

    @Test
    void objetInconnuProvoqueUneErreur() {
        given(objets.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.recommanderValorisation(new ValorisationRequest(9L, null)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("9");
    }

    @Test
    void demandeSansObjetRejetee() {
        assertThatThrownBy(() -> service.recommanderValorisation(new ValorisationRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("objetId");
    }

    @Test
    void feedbackSurRecommandationInconnueRejete() {
        given(repository.findHistorique(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.enregistrerFeedback(new FeedbackRequest(99L, true, "clair")))
                .isInstanceOf(NoSuchElementException.class);

        verify(repository, never()).insertFeedback(anyLong(), anyBoolean(), any());
    }

    @Test
    void feedbackEnregistre() {
        given(repository.findHistorique(5L)).willReturn(Optional.of(new RecommandationHistorique(
                5L, 1L, "VALORISATION", "{}", "{}", "ML", "v1", OffsetDateTime.now())));
        given(repository.insertFeedback(5L, true, "clair")).willReturn(11L);

        FeedbackResponse reponse = service.enregistrerFeedback(new FeedbackRequest(5L, true, "clair"));

        assertThat(reponse.id()).isEqualTo(11L);
        assertThat(reponse.recommandationId()).isEqualTo(5L);
        assertThat(reponse.utile()).isTrue();
        assertThat(reponse.enregistreLe()).isNotNull();
        verify(repository).insertFeedback(5L, true, "clair");
    }

    @Test
    void metadataVientDuMlQuandIlEstDisponible() {
        given(ml.metadata()).willReturn(new ModeleMetadata(true, SourcePrediction.ML,
                "valorisation-gbm-2.0.0", "acteurs-gbr-2.0.0", "marche-tfidf-2.0.0",
                Map.of("accuracy", 0.94), List.of("coutRatio", "etat")));

        ModeleMetadata metadata = service.metadata();

        assertThat(metadata.actif()).isTrue();
        assertThat(metadata.source()).isEqualTo(SourcePrediction.ML);
        assertThat(metadata.versionValorisation()).isEqualTo("valorisation-gbm-2.0.0");
        assertThat(metadata.versionActeurs()).isEqualTo("acteurs-gbr-2.0.0");
        assertThat(metadata.metriques()).containsEntry("accuracy", 0.94);
        assertThat(metadata.topFeatures()).containsExactly("coutRatio", "etat");
    }

    @Test
    void metadataLocaleQuandLeMlEstAbsent() {
        willThrow(new MlIndisponibleException("ML injoignable")).given(ml).metadata();
        given(repository.findModeleMetadata()).willReturn(List.of(
                new ModeleMetadataEntity(1L, "valorisation", "valorisation-gbm-3.1.0", true,
                        "{\"accuracy\":0.91}", "coutRatio,etat", OffsetDateTime.now()),
                new ModeleMetadataEntity(2L, "acteurs", "acteurs-gbr-3.1.0", false,
                        null, null, OffsetDateTime.now())));

        ModeleMetadata metadata = service.metadata();

        assertThat(metadata.actif()).isFalse();
        assertThat(metadata.source()).isEqualTo(SourcePrediction.HEURISTIQUE);
        assertThat(metadata.versionValorisation()).isEqualTo("valorisation-gbm-3.1.0");
        assertThat(metadata.versionActeurs()).isEqualTo("acteurs-gbr-3.1.0");
        assertThat(metadata.metriques()).containsEntry("accuracy", 0.91);
        assertThat(metadata.topFeatures()).containsExactly("coutRatio", "etat");
    }

    @Test
    void acteursRepliqueHeuristiqueQuandMlAbsent() {
        willThrow(new MlIndisponibleException("ML injoignable")).given(ml).predictActeurs(any());
        given(acteurs.findAll(TypeActeur.REPARATEUR)).willReturn(List.of(
                new ActeurRef(1L, 1L, TypeActeur.REPARATEUR, "Atelier Ben Salah", "Tunis",
                        36.8065, 10.1815, 4.6, 18, true, "electronique", "1", List.of()),
                new ActeurRef(2L, 2L, TypeActeur.REPARATEUR, "Repar Sfax", "Sfax",
                        34.7406, 10.76, 4.2, 7, true, "electronique", "1", List.of())));
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(3L);

        ActeursRequest request = new ActeursRequest(
                TypeActeur.REPARATEUR, null, 36.8065, 10.1815, 100.0, null);
        RecoActeursResponse reponse = service.recommanderActeurs(request);

        assertThat(reponse.source()).isEqualTo(SourcePrediction.HEURISTIQUE);
        assertThat(reponse.typeActeur()).isEqualTo(TypeActeur.REPARATEUR);
        assertThat(reponse.classement()).isNotEmpty();
        assertThat(reponse.classement()).isSortedAccordingTo(
                (a, b) -> Double.compare(b.score(), a.score()));
        verify(repository).insertHistorique(isNull(), eq("ACTEURS"), anyString(),
                anyString(), eq("HEURISTIQUE"), eq(HeuristicRecoProvider.VERSION_HEURISTIQUE));
    }

    @Test
    void marcheRepliqueHeuristiqueQuandMlAbsent() {
        willThrow(new MlIndisponibleException("ML injoignable")).given(ml).predictMarche(any());
        given(marche.findAll()).willReturn(List.of(
                new ArticleMarcheRef(1L, "Velo de ville reconditionne", "Velo & Mobilite",
                        "BON", 90.0, "Velo de ville ancien restaure en bon etat."),
                new ArticleMarcheRef(2L, "Casque audio sans fil", "Electronique",
                        "NEUF", 200.0, "Casque audio sans fil haute qualite.")));
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(4L);

        MarcheRequest request = new MarcheRequest(
                new ArticleMarche(null, "Velo de ville", "Velo & Mobilite", "BON", 150.0, null),
                null, null);
        RecoMarcheResponse reponse = service.articlesSimilaires(request);

        assertThat(reponse.source()).isEqualTo(SourcePrediction.HEURISTIQUE);
        assertThat(reponse.articles()).isNotEmpty();
        assertThat(reponse.articles().get(0).equitePrix())
                .isIn("EXCELLENT_DEAL", "FAIR_MARKET", "OVERPRICED");
        verify(repository).insertHistorique(isNull(), eq("MARCHE"), anyString(),
                anyString(), eq("HEURISTIQUE"), eq(HeuristicRecoProvider.VERSION_HEURISTIQUE));
    }

    @Test
    void marchePreferLesAnnoncesLiveDuMarketplace() {
        willThrow(new MlIndisponibleException("ML injoignable")).given(ml).predictMarche(any());
        given(marketplace.findAllDisponibles()).willReturn(List.of(
                new ArticleMarche(55L, "iPhone 13 reconditionne", "Electronique",
                        "EXCELLENT", 320.0, "iPhone 13 reconditionne sous garantie.")));
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(8L);

        MarcheRequest request = new MarcheRequest(
                new ArticleMarche(null, "iPhone 12", "Electronique", "BON", 250.0, null),
                null, null);
        RecoMarcheResponse reponse = service.articlesSimilaires(request);

        assertThat(reponse.articles()).isNotEmpty();
        assertThat(reponse.articles().get(0).articleId()).isEqualTo(55L);
        verify(marche, never()).findAll();
    }

    @Test
    void marcheUtiliseLePortMarcheQuandMarketplaceNeRetourneRien() {
        willThrow(new MlIndisponibleException("ML injoignable")).given(ml).predictMarche(any());
        given(marketplace.findAllDisponibles()).willReturn(List.of());
        given(marche.findAll()).willReturn(List.of(
                new ArticleMarcheRef(1L, "Velo de ville reconditionne", "Velo & Mobilite",
                        "BON", 90.0, "Velo de ville ancien restaure en bon etat.")));
        given(repository.insertHistorique(any(), any(), any(), any(), any(), any()))
                .willReturn(9L);

        MarcheRequest request = new MarcheRequest(
                new ArticleMarche(null, "Velo de ville", "Velo & Mobilite", "BON", 150.0, null),
                null, null);
        RecoMarcheResponse reponse = service.articlesSimilaires(request);

        assertThat(reponse.articles()).isNotEmpty();
        assertThat(reponse.articles().get(0).articleId()).isEqualTo(1L);
        verify(marche).findAll();
    }

    @Test
    void historiqueTronqueLesChargesUtilesTropLongues() {
        given(repository.listHistorique(1L)).willReturn(List.of(new RecommandationHistorique(
                5L, 1L, "VALORISATION", "x".repeat(2000), "{}", "ML", "v1", OffsetDateTime.now())));

        List<HistoriqueReco> historique = service.historique(1L);

        assertThat(historique).hasSize(1);
        assertThat(historique.get(0).chargeUtile()).hasSize(1501);
        assertThat(historique.get(0).chargeUtile()).endsWith("…");
        assertThat(historique.get(0).source()).isEqualTo(SourcePrediction.ML);
    }

    @Test
    void expliquerRecommandationInconnue() {
        given(repository.findHistorique(4L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.expliquer(4L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("4");
    }
}
