package com.valorio.marketplace.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.domain.MarketplaceOffer;
import com.valorio.marketplace.domain.OfferStatus;
import com.valorio.marketplace.service.MarketplaceOfferService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MarketplaceOfferController.class)
class MarketplaceOfferControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MarketplaceOfferService service;

    @Test
    void listeLesOffres() throws Exception {
        given(service.toutesLesOffres()).willReturn(List.of(offre(1L, OfferStatus.PENDING)));

        mvc.perform(get("/api/marketplace/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerName").value("Lucas Moreau"))
                .andExpect(jsonPath("$[0].item.title").value("iPhone 13"));
    }

    @Test
    void creeUneOffre() throws Exception {
        given(service.creer(eq(1L), any())).willReturn(offre(5L, OfferStatus.PENDING));

        mvc.perform(post("/api/marketplace/offers/item/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buyerName\":\"Lucas Moreau\",\"buyerEmail\":\"lucas@mail.fr\",\"offeredPrice\":270}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void changeLeStatutDUneOffre() throws Exception {
        given(service.changerStatut(eq(1L), eq(OfferStatus.ACCEPTED)))
                .willReturn(offre(1L, OfferStatus.ACCEPTED));

        mvc.perform(patch("/api/marketplace/offers/1/status").param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void supprimeUneOffre() throws Exception {
        mvc.perform(delete("/api/marketplace/offers/1"))
                .andExpect(status().isNoContent());
    }

    private static MarketplaceOffer offre(long id, OfferStatus statut) {
        MarketplaceItem item = new MarketplaceItem(1L, "iPhone 13", "desc", Category.ELECTRONICS,
                ItemCondition.GOOD, ItemType.SALE, ItemStatus.AVAILABLE, new BigDecimal("290.00"), null, null,
                null, null, null, null, null, null, null, null, null, null, 120, 42,
                OffsetDateTime.now(), OffsetDateTime.now());
        return new MarketplaceOffer(id, item, "Lucas Moreau", "lucas@mail.fr", null,
                new BigDecimal("270.00"), "Bonjour", statut, OffsetDateTime.now());
    }
}
