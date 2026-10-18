package com.valorio.marketplace.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.dto.AnomalyDetectionResult;
import com.valorio.marketplace.dto.EstimatePriceResponse;
import com.valorio.marketplace.dto.GenerateListingResponse;
import com.valorio.marketplace.dto.ModerationResponse;
import com.valorio.marketplace.service.AiService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiAssistantController.class)
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AiService ia;

    @Test
    void genereUneAnnonce() throws Exception {
        given(ia.generateListing(any())).willReturn(new GenerateListingResponse(
                "iPhone 13", "### Description", Category.ELECTRONICS, ItemCondition.GOOD,
                new BigDecimal("163"), new BigDecimal("122"), new BigDecimal("204"),
                List.of("electronics"), "résumé", 0.92));

        mvc.perform(post("/api/marketplace/ai/generate-listing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"iPhone 13 rayé\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("iPhone 13"))
                .andExpect(jsonPath("$.suggestedCategory").value("ELECTRONICS"));
    }

    @Test
    void estimeUnPrix() throws Exception {
        given(ia.estimatePrice(any())).willReturn(new EstimatePriceResponse(
                new BigDecimal("163"), new BigDecimal("122"), new BigDecimal("204"),
                new BigDecimal("35.0"), "STANDARD_MARKET", "rationale", List.of("facteur"), 0.89));

        mvc.perform(post("/api/marketplace/ai/estimate-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"ELECTRONICS\",\"condition\":\"GOOD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedPrice").value(163));
    }

    @Test
    void modereUnContenu() throws Exception {
        given(ia.moderateContent(any())).willReturn(new ModerationResponse(
                false, 38.0, "HIGH", List.of("mot-clé sensible"), "à réviser"));

        mvc.perform(post("/api/marketplace/ai/moderate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Passeport trouvé\",\"description\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false));
    }

    @Test
    void detecteUneAnomalie() throws Exception {
        given(ia.detectAnomaly(any())).willReturn(new AnomalyDetectionResult(
                true, "ZERO_PRICE_SALE", 0.70, new BigDecimal("122"), new BigDecimal("204"),
                BigDecimal.ZERO, "message", List.of("conseil")));

        mvc.perform(post("/api/marketplace/ai/detect-anomaly")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"iPhone 13\",\"type\":\"SALE\",\"price\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anomalyDetected").value(true));
    }

    @Test
    void exposeLaSanteDuMoteurIA() throws Exception {
        mvc.perform(get("/api/marketplace/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
