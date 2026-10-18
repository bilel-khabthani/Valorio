package com.valorio.marketplace.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.MarketplaceStats;
import com.valorio.marketplace.service.MarketplaceItemService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MarketplaceItemController.class)
class MarketplaceItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MarketplaceItemService service;

    @Test
    void listeLesArticles() throws Exception {
        given(service.rechercher(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .willReturn(List.of(article(1L, ItemStatus.AVAILABLE)));

        mvc.perform(get("/api/marketplace/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("iPhone 13"))
                .andExpect(jsonPath("$[0].category").value("ELECTRONICS"));
    }

    @Test
    void consulteUnArticle() throws Exception {
        given(service.consulter(1L)).willReturn(article(1L, ItemStatus.AVAILABLE));

        mvc.perform(get("/api/marketplace/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(290.00));
    }

    @Test
    void creeUnArticle() throws Exception {
        given(service.creer(any())).willReturn(article(9L, ItemStatus.AVAILABLE));

        mvc.perform(post("/api/marketplace/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"iPhone 13\",\"description\":\"desc\",\"category\":\"ELECTRONICS\","
                                + "\"condition\":\"GOOD\",\"type\":\"SALE\",\"price\":290}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void valideLaRequeteDeCreation() throws Exception {
        mvc.perform(post("/api/marketplace/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retourneLesStatistiques() throws Exception {
        given(service.statistiques()).willReturn(new MarketplaceStats(
                6, 5, 1, 1, 98.2, 98.5, Map.of("ELECTRONICS", 2L), Map.of("SALE", 4L)));

        mvc.perform(get("/api/marketplace/items/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(6))
                .andExpect(jsonPath("$.pendingOffers").value(1));
    }

    private static MarketplaceItem article(long id, ItemStatus statut) {
        return new MarketplaceItem(id, "iPhone 13", "desc", Category.ELECTRONICS, ItemCondition.GOOD,
                ItemType.SALE, statut, new BigDecimal("290.00"), new BigDecimal("295.00"), "FAIR_MARKET",
                98.5, "ok", null, "Tunis", null, "Regie", "a@b.fr", null, null, "apple", 120, 42,
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
