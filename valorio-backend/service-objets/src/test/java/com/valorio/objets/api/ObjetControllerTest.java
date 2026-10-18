package com.valorio.objets.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.EvenementHistorique;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.StatutObjet;
import com.valorio.objets.domain.TypeEvenement;
import com.valorio.objets.service.ObjetService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ObjetController.class)
class ObjetControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ObjetService service;

    @Test
    void listeLesObjets() throws Exception {
        given(service.lister(isNull(), isNull(), isNull(), isNull(), isNull()))
                .willReturn(List.of(objet(1L, StatutObjet.DECLARE)));

        mvc.perform(get("/api/objets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].intitule").value("Table en bois"));
    }

    @Test
    void changeLeStatutDUnObjet() throws Exception {
        given(service.changerStatut(eq(1L), eq(StatutObjet.DISPONIBLE), isNull(), isNull()))
                .willReturn(objet(1L, StatutObjet.DISPONIBLE));

        mvc.perform(put("/api/objets/1/statut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"DISPONIBLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("DISPONIBLE"));
    }

    @Test
    void refuseUneTransitionInterdite() throws Exception {
        given(service.changerStatut(eq(1L), eq(StatutObjet.DECLARE), isNull(), isNull()))
                .willThrow(new IllegalStateException("Transition interdite : VENDU -> DECLARE"));

        mvc.perform(put("/api/objets/1/statut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"DECLARE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void recoitUnEvenementDeCollaborationInterServices() throws Exception {
        EvenementHistorique evenement = new EvenementHistorique(9L, 1L, TypeEvenement.DON,
                "Annonce de don publiée", 1L, StatutObjet.DONNE, OffsetDateTime.now());
        given(service.enregistrerEvenement(eq(1L), eq(TypeEvenement.DON),
                eq("Annonce de don publiée"), eq(1L), eq(StatutObjet.DONNE)))
                .willReturn(evenement);

        mvc.perform(post("/api/objets/1/historique")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DON\",\"libelle\":\"Annonce de don publiée\","
                                + "\"acteurId\":1,\"statutCible\":\"DONNE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DON"))
                .andExpect(jsonPath("$.statutCible").value("DONNE"));
    }

    @Test
    void valideLaRequeteDeDeclaration() throws Exception {
        mvc.perform(post("/api/objets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intitule\":\"\",\"categorieId\":1}"))
                .andExpect(status().isBadRequest());
    }

    private static Objet objet(long id, StatutObjet statut) {
        return new Objet(id, "Table en bois", null, 2L, EtatObjet.BON, 5, 120.0, "Tunis",
                null, null, List.of("bois"), 1L, statut, List.of(),
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
