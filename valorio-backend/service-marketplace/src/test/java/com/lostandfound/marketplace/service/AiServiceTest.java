package com.lostandfound.marketplace.service;

import com.lostandfound.marketplace.dto.*;
import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemCondition;
import com.lostandfound.marketplace.model.ItemType;
import com.lostandfound.marketplace.model.MarketplaceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests Unitaires et de Robustesse IA (Module Marketplace)")
class AiServiceTest {

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService();
    }

    @Test
    @DisplayName("Génération IA d'une annonce à partir d'un prompt brut")
    void testGenerateListingFromRawPrompt() {
        GenerateListingRequest request = new GenerateListingRequest();
        request.setPrompt("iPhone 13 128go noir avec câble");
        request.setCategory(Category.ELECTRONICS);
        request.setCondition(ItemCondition.GOOD);
        request.setType(ItemType.SALE);
        request.setLocation("Bureau Objets Trouvés Gare");

        GenerateListingResponse response = aiService.generateListing(request);

        assertNotNull(response);
        assertNotNull(response.getTitle());
        assertTrue(response.getTitle().toLowerCase().contains("iphone 13"));
        assertTrue(response.getDescription().contains("Bureau Objets Trouvés Gare"));
        assertTrue(response.getSuggestedPrice().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(response.getTags().isEmpty());
        assertTrue(response.getTags().contains("verifie-ia"));
        assertEquals(0.92, response.getConfidenceScore(), 0.05);
    }

    @Test
    @DisplayName("Modèle d'estimation de prix avec coefficient d'usure")
    void testPriceEstimationWithDepreciation() {
        EstimatePriceRequest reqNew = new EstimatePriceRequest();
        reqNew.setTitle("Montre Tissot");
        reqNew.setCategory(Category.JEWELRY_WATCHES);
        reqNew.setCondition(ItemCondition.NEW_UNOPENED);

        EstimatePriceResponse resNew = aiService.estimatePrice(reqNew);

        EstimatePriceRequest reqUsed = new EstimatePriceRequest();
        reqUsed.setTitle("Montre Tissot");
        reqUsed.setCategory(Category.JEWELRY_WATCHES);
        reqUsed.setCondition(ItemCondition.FAIR);

        EstimatePriceResponse resUsed = aiService.estimatePrice(reqUsed);

        assertTrue(resNew.getRecommendedPrice().compareTo(resUsed.getRecommendedPrice()) > 0,
                "L'estimation pour du neuf doit être supérieure à l'état moyen");
        assertTrue(resUsed.getEstimatedDepreciationPercent().doubleValue() > 0);
        assertNotNull(resNew.getRationale());
        assertFalse(resNew.getMarketFactors().isEmpty());
    }

    @Test
    @DisplayName("Modération IA : Blocage de documents d'identité interdits à la revente")
    void testModerationBlocksPassportOrIdentitySale() {
        ModerationRequest request = new ModerationRequest(
                "Passeport français trouvé à vendre",
                "Passeport biométrique trouvé dans le parc, proposé au plus offrant",
                Category.DOCUMENTS_KEYS,
                "50.00"
        );

        ModerationResponse response = aiService.moderateContent(request);

        assertFalse(response.isApproved(), "La revente d'un passeport doit être strictement bloquée par l'IA");
        assertEquals("BLOCKED", response.getRiskLevel());
        assertFalse(response.getFlaggedIssues().isEmpty());
        assertTrue(response.getFlaggedIssues().stream().anyMatch(i -> i.contains("Violation légale") || i.contains("passeport")));
    }

    @Test
    @DisplayName("Modération IA : Validation d'un objet légitime")
    void testModerationApprovesLegitimateItem() {
        ModerationRequest request = new ModerationRequest(
                "Guitare acoustique Yamaha F310",
                "Guitare non réclamée après 6 mois, bon état, cordes changées.",
                Category.SPORTS_LEISURE,
                "70.00"
        );

        ModerationResponse response = aiService.moderateContent(request);

        assertTrue(response.isApproved());
        assertEquals("LOW", response.getRiskLevel());
        assertTrue(response.getSafetyScore() >= 90.0);
    }

    @Test
    @DisplayName("Détection d'anomalie : Don solidaire avec prix supérieur à zéro")
    void testAnomalyDetectionDonationWithPrice() {
        MarketplaceItem item = new MarketplaceItem();
        item.setTitle("Veste solidaire");
        item.setCategory(Category.CLOTHING_ACCESSORIES);
        item.setCondition(ItemCondition.GOOD);
        item.setType(ItemType.DONATION);
        item.setPrice(new BigDecimal("25.00")); // Anomalie : Don avec prix > 0

        AnomalyDetectionResult result = aiService.detectAnomaly(item);

        assertTrue(result.isAnomalyDetected());
        assertEquals("PRICE_TYPE_MISMATCH", result.getAnomalyType());
        assertTrue(result.getAnomalyScore() > 0.8);
    }

    @Test
    @DisplayName("Détection d'anomalie : Prix anormalement bas pour un équipement high-tech")
    void testAnomalyDetectionPriceAbnormallyLow() {
        MarketplaceItem item = new MarketplaceItem();
        item.setTitle("Apple iPhone 15 Pro Max");
        item.setCategory(Category.ELECTRONICS);
        item.setCondition(ItemCondition.EXCELLENT);
        item.setType(ItemType.SALE);
        item.setPrice(new BigDecimal("2.00")); // Anomalie : iPhone 15 à 2€

        AnomalyDetectionResult result = aiService.detectAnomaly(item);

        assertTrue(result.isAnomalyDetected());
        assertEquals("PRICE_ABNORMALLY_LOW", result.getAnomalyType());
    }
}
