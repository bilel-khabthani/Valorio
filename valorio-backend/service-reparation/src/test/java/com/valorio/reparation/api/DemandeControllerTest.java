package com.valorio.reparation.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.reparation.domain.DemandeReparation;
import com.valorio.reparation.domain.StatutDemande;
import com.valorio.reparation.service.ReparationService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DemandeController.class)
class DemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReparationService service;

    private static DemandeReparation demande(OffsetDateTime intervention) {
        return new DemandeReparation(7L, 1L, 2L, null, "Écran cassé", StatutDemande.ACCEPTEE,
                intervention, OffsetDateTime.now(), OffsetDateTime.now(), List.of());
    }

    @Test
    void planifieUneIntervention() throws Exception {
        OffsetDateTime date = OffsetDateTime.parse("2026-06-01T10:00:00Z");
        given(service.planifierIntervention(eq(7L), any(OffsetDateTime.class)))
                .willReturn(demande(date));

        mvc.perform(put("/api/demandes/7/intervention")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateIntervention\":\"2026-06-01T10:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"))
                .andExpect(jsonPath("$.dateIntervention").exists());
    }

    @Test
    void refuseUneDateManquante() throws Exception {
        mvc.perform(put("/api/demandes/7/intervention")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
