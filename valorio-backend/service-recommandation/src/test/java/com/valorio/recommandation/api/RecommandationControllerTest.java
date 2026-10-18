package com.valorio.recommandation.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recommandation.domain.RouteValorisation;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.dto.AlternativeReco;
import com.valorio.recommandation.dto.FeedbackResponse;
import com.valorio.recommandation.dto.ModeleMetadata;
import com.valorio.recommandation.dto.RaisonReco;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import com.valorio.recommandation.service.RecommandationService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommandationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommandationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private RecommandationService service;

    @Test
    void valorisationRetourne200() throws Exception {
        RecoValorisationResponse reponse = new RecoValorisationResponse(42L,
                RouteValorisation.REVENTE, 0.75,
                List.of(new AlternativeReco(RouteValorisation.DON, 0.15)),
                List.of(new RaisonReco("etat", "NEUF", 0.9, "Objet neuf recent")),
                0.8, SourcePrediction.HEURISTIQUE, "heuristique-1.0.0");
        given(service.recommanderValorisation(any())).willReturn(reponse);

        mvc.perform(post("/api/recommandations/valorisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("objetId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.routeRecommandee").value("REVENTE"))
                .andExpect(jsonPath("$.probabilite").value(0.75))
                .andExpect(jsonPath("$.source").value("HEURISTIQUE"))
                .andExpect(jsonPath("$.modeleVersion").value("heuristique-1.0.0"))
                .andExpect(jsonPath("$.raisons[0].feature").value("etat"));
    }

    @Test
    void valorisationCorpsVideRetourne400() throws Exception {
        given(service.recommanderValorisation(any()))
                .willThrow(new IllegalArgumentException(
                        "Un objetId ou une description objet inline est obligatoire"));

        mvc.perform(post("/api/recommandations/valorisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"))
                .andExpect(jsonPath("$.detail").value(
                        "Un objetId ou une description objet inline est obligatoire"));
    }

    @Test
    void acteursSansTypeRetourne400DeValidation() throws Exception {
        mvc.perform(post("/api/recommandations/acteurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation échouée"));
    }

    @Test
    void metadataRetourne200() throws Exception {
        given(service.metadata()).willReturn(new ModeleMetadata(false, SourcePrediction.HEURISTIQUE,
                "valorisation-gbm-1.0.0", "acteurs-gbr-1.0.0", "marche-tfidf-1.0.0",
                Map.of("accuracy", 0.91), List.of("coutRatio", "etat")));

        mvc.perform(get("/api/recommandations/modele/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(false))
                .andExpect(jsonPath("$.source").value("HEURISTIQUE"))
                .andExpect(jsonPath("$.versionValorisation").value("valorisation-gbm-1.0.0"))
                .andExpect(jsonPath("$.metriques.accuracy").value(0.91))
                .andExpect(jsonPath("$.topFeatures[0]").value("coutRatio"));
    }

    @Test
    void feedbackRetourne201() throws Exception {
        given(service.enregistrerFeedback(any())).willReturn(new FeedbackResponse(
                7L, 5L, true, OffsetDateTime.parse("2026-10-18T10:15:30Z")));

        mvc.perform(post("/api/recommandations/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommandationId\":5,\"utile\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.recommandationId").value(5))
                .andExpect(jsonPath("$.utile").value(true));
    }

    @Test
    void explicationInconnueRetourne404() throws Exception {
        given(service.expliquer(4L))
                .willThrow(new NoSuchElementException("Recommandation 4 introuvable"));

        mvc.perform(post("/api/recommandations/4/explication"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"));
    }
}
