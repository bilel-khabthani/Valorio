package com.valorio.don.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.don.domain.AnnonceDon;
import com.valorio.don.domain.StatutAnnonceDon;
import com.valorio.don.service.DonService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DonController.class)
class DonControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DonService service;

    @Test
    void annuleUneAnnonce() throws Exception {
        given(service.annuler(eq(3L), isNull()))
                .willReturn(new AnnonceDon(3L, 1L, 5L, "Table", null, null, "Tunis",
                        null, null, StatutAnnonceDon.ANNULEE, OffsetDateTime.now()));

        mvc.perform(put("/api/dons/3/annuler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }
}
