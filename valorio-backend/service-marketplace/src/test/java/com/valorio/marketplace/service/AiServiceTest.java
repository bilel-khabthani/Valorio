package com.valorio.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.EstimatePriceRequest;
import com.valorio.marketplace.dto.EstimatePriceResponse;
import com.valorio.marketplace.dto.GenerateListingRequest;
import com.valorio.marketplace.dto.GenerateListingResponse;
import com.valorio.marketplace.dto.ModerationRequest;
import com.valorio.marketplace.dto.ModerationResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiServiceTest {

    private final AiService ia = new AiService();

    @Test
    void genereUneAnnonceAvecTitreEtPrix() {
        GenerateListingResponse reponse = ia.generateListing(
                new GenerateListingRequest("iPhone 13 rayé", null, null, null, null, "Tunis"));

        assertThat(reponse.title()).contains("iPhone 13");
        assertThat(reponse.suggestedCategory()).isEqualTo(Category.ELECTRONICS);
        assertThat(reponse.suggestedPrice()).isPositive();
        assertThat(reponse.tags()).contains("objet-trouve");
    }

    @Test
    void genereUnDonGratuit() {
        GenerateListingResponse reponse = ia.generateListing(
                new GenerateListingRequest("manteau hiver", null, null, ItemType.DONATION, null, null));

        assertThat(reponse.suggestedPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reponse.title()).contains("Don Solidaire");
    }

    @Test
    void estimeUnPrixSelonCategorieEtEtat() {
        EstimatePriceResponse reponse = ia.estimatePrice(
                new EstimatePriceRequest("Ordinateur portable", Category.ELECTRONICS, ItemCondition.GOOD, null, null, null));

        assertThat(reponse.recommendedPrice()).isEqualByComparingTo(new BigDecimal("163"));
        assertThat(reponse.minFairPrice()).isLessThan(reponse.maxFairPrice());
        assertThat(reponse.pricingTier()).isEqualTo("STANDARD_MARKET");
    }

    @Test
    void refuseLesContenusProhibes() {
        ModerationResponse reponse = ia.moderateContent(
                new ModerationRequest("Passeport trouvé", "carte d'identite", Category.DOCUMENTS_KEYS, "10"));

        assertThat(reponse.approved()).isFalse();
        assertThat(reponse.flaggedIssues()).isNotEmpty();
    }

    @Test
    void detecteUneAnomalieDePrixSurUnDon() {
        MarketplaceItem don = item(ItemType.DONATION, new BigDecimal("25.00"));
        assertThat(ia.detectAnomaly(don).anomalyDetected()).isTrue();

        MarketplaceItem vente = item(ItemType.SALE, new BigDecimal("163.00"));
        assertThat(ia.detectAnomaly(vente).anomalyDetected()).isFalse();
    }

    private static MarketplaceItem item(ItemType type, BigDecimal prix) {
        return new MarketplaceItem(1L, "iPhone 13", "desc", Category.ELECTRONICS, ItemCondition.GOOD,
                type, ItemStatus.AVAILABLE, prix, null, null, null, null, null, null, null, null,
                null, null, null, null, 90, 0, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
