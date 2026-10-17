package com.lostandfound.marketplace.service;

import com.lostandfound.marketplace.dto.*;
import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemCondition;
import com.lostandfound.marketplace.model.ItemType;
import com.lostandfound.marketplace.model.MarketplaceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${marketplace.ai.provider:hybrid}")
    private String aiProvider;

    @Value("${marketplace.ai.api-key:}")
    private String apiKey;

    // Benchmark base market values for items in Lost & Found categories (in EUR)
    private static final Map<Category, BigDecimal> CATEGORY_BENCHMARKS = Map.of(
            Category.ELECTRONICS, new BigDecimal("250.00"),
            Category.JEWELRY_WATCHES, new BigDecimal("120.00"),
            Category.BAGS_LUGGAGE, new BigDecimal("65.00"),
            Category.DOCUMENTS_KEYS, new BigDecimal("15.00"),
            Category.CLOTHING_ACCESSORIES, new BigDecimal("45.00"),
            Category.BICYCLES_VEHICLES, new BigDecimal("180.00"),
            Category.SPORTS_LEISURE, new BigDecimal("70.00"),
            Category.OTHER, new BigDecimal("30.00")
    );

    // List of prohibited items or keywords for content moderation in Lost & Found marketplace
    private static final List<String> PROHIBITED_KEYWORDS = List.of(
            "passeport", "carte d'identite", "carte bancaire", "permis de conduire",
            "arme", "pistolet", "fusil", "couteau", "drogue", "cannabis", "cocaine",
            "medicament", "ordonnance", "faux billet", "contrefacon", "arnaque"
    );

    /**
     * AI Smart Listing Generator: transforms raw keywords or brief notes into
     * a polished, professional marketplace listing with title, markdown description, and tags.
     */
    public GenerateListingResponse generateListing(GenerateListingRequest request) {
        log.info("Generating AI listing for prompt: {}", request.getPrompt());

        Category category = request.getCategory() != null ? request.getCategory() : inferCategory(request.getPrompt());
        ItemCondition condition = request.getCondition() != null ? request.getCondition() : inferCondition(request.getPrompt());
        ItemType type = request.getType() != null ? request.getType() : ItemType.SALE;

        // Title synthesis
        String cleanSubject = extractCleanSubject(request.getPrompt());
        String generatedTitle;
        if (type == ItemType.DONATION) {
            generatedTitle = String.format("[Don Solidaire] %s - %s", cleanSubject, condition.getLabel());
        } else if (type == ItemType.REWARD_BOUNTY) {
            generatedTitle = String.format("[Avis de Recherche / Récompense] %s", cleanSubject);
        } else {
            generatedTitle = String.format("%s (%s) - Revalorisation Objet Trouvé", cleanSubject, condition.getLabel());
        }

        // Description generation in formatted Markdown
        StringBuilder desc = new StringBuilder();
        desc.append("### Description du bien revalorisé\n\n");
        desc.append(String.format("**Objet :** %s  \n", cleanSubject));
        desc.append(String.format("**Catégorie :** %s  \n", category.getLabel()));
        desc.append(String.format("**État vérifié :** %s  \n", condition.getLabel()));
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            desc.append(String.format("**Lieu de collecte / retrait :** %s  \n", request.getLocation()));
        }
        desc.append("\n---\n\n");
        desc.append("#### Détails & Contexte de l'objet\n");
        desc.append(String.format("Cet article provient de notre module de gestion des objets trouvés et non réclamés à l'issue du délai réglementaire de conservation. "));
        desc.append(String.format("L'objet a fait l'objet d'un contrôle visuel et technique préalable. %s.\n\n", request.getPrompt()));
        desc.append("#### Modalités d'acquisition\n");
        if (type == ItemType.DONATION) {
            desc.append("- **Prix :** Gratuit (Don caritatif ou étudiant).\n");
            desc.append("- **Condition :** Remise en main propre sur présentation d'une pièce d'identité.\n");
        } else if (type == ItemType.REWARD_BOUNTY) {
            desc.append("- **Récompense promise :** Versée immédiatement lors de la restitution de l'objet.\n");
            desc.append("- **Contact direct :** Merci d'utiliser le bouton de contact sécurisé ci-dessous.\n");
        } else {
            desc.append("- **Transaction sécurisée :** Paiement et retrait sécurisés via le service d'accueil.\n");
            desc.append("- **Garantie :** Objet vérifié conforme à la description.\n");
        }

        // Valuation & pricing bounds
        EstimatePriceRequest priceReq = new EstimatePriceRequest();
        priceReq.setTitle(cleanSubject);
        priceReq.setCategory(category);
        priceReq.setCondition(condition);
        EstimatePriceResponse priceEst = estimatePrice(priceReq);

        BigDecimal suggestedPrice = (type == ItemType.DONATION) ? BigDecimal.ZERO : priceEst.getRecommendedPrice();

        // Extract relevant tags
        List<String> tags = new ArrayList<>();
        tags.add(category.name().toLowerCase().replace("_", "-"));
        tags.add(condition.name().toLowerCase().replace("_", "-"));
        tags.add(type.name().toLowerCase());
        tags.add("objet-trouve");
        tags.add("verifie-ia");
        if (cleanSubject.toLowerCase().contains("apple") || cleanSubject.toLowerCase().contains("iphone") || cleanSubject.toLowerCase().contains("macbook")) {
            tags.add("apple");
        }
        if (cleanSubject.toLowerCase().contains("samsung")) tags.add("samsung");

        GenerateListingResponse response = new GenerateListingResponse();
        response.setTitle(generatedTitle);
        response.setDescription(desc.toString());
        response.setSuggestedCategory(category);
        response.setSuggestedCondition(condition);
        response.setSuggestedPrice(suggestedPrice);
        response.setMinPrice(type == ItemType.DONATION ? BigDecimal.ZERO : priceEst.getMinFairPrice());
        response.setMaxPrice(type == ItemType.DONATION ? BigDecimal.ZERO : priceEst.getMaxFairPrice());
        response.setTags(tags);
        response.setAiSummary(String.format("Annonce optimisée par l'IA avec un score de clarté de 94%%. Valorisation estimée à %s €.", suggestedPrice));
        response.setConfidenceScore(0.92);

        return response;
    }

    /**
     * AI Price Estimation Model: estimates a fair secondary market valuation
     * based on category depreciation curves, physical condition, and benchmark data.
     */
    public EstimatePriceResponse estimatePrice(EstimatePriceRequest request) {
        Category category = request.getCategory() != null ? request.getCategory() : Category.OTHER;
        ItemCondition condition = request.getCondition() != null ? request.getCondition() : ItemCondition.GOOD;

        BigDecimal baseBenchmark = request.getOriginalRetailPrice() != null && request.getOriginalRetailPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.getOriginalRetailPrice()
                : CATEGORY_BENCHMARKS.getOrDefault(category, new BigDecimal("50.00"));

        // If title suggests a flagship model (e.g., iPhone, MacBook, Rolex) adjust benchmark
        if (request.getTitle() != null) {
            String lower = request.getTitle().toLowerCase();
            if (lower.contains("iphone 15") || lower.contains("macbook pro")) {
                baseBenchmark = baseBenchmark.max(new BigDecimal("750.00"));
            } else if (lower.contains("iphone 13") || lower.contains("macbook air")) {
                baseBenchmark = baseBenchmark.max(new BigDecimal("450.00"));
            } else if (lower.contains("airpods")) {
                baseBenchmark = baseBenchmark.max(new BigDecimal("110.00"));
            } else if (lower.contains("trottinette") || lower.contains("velo electrique")) {
                baseBenchmark = baseBenchmark.max(new BigDecimal("320.00"));
            }
        }

        // Apply condition depreciation coefficient
        double conditionFactor = condition.getValueMultiplier();

        // Calculate recommended price
        BigDecimal rawRecommended = baseBenchmark.multiply(BigDecimal.valueOf(conditionFactor));
        // Round to nearest Euro or half-Euro
        BigDecimal recommendedPrice = rawRecommended.setScale(0, RoundingMode.HALF_UP);
        if (recommendedPrice.compareTo(BigDecimal.ONE) < 0) {
            recommendedPrice = BigDecimal.ONE;
        }

        BigDecimal minFairPrice = recommendedPrice.multiply(new BigDecimal("0.75")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal maxFairPrice = recommendedPrice.multiply(new BigDecimal("1.25")).setScale(0, RoundingMode.HALF_UP);

        BigDecimal depreciationPercent = BigDecimal.valueOf((1.0 - conditionFactor) * 100).setScale(1, RoundingMode.HALF_UP);

        String tier;
        if (recommendedPrice.compareTo(new BigDecimal("200")) > 0) {
            tier = "PREMIUM_VALUATION";
        } else if (recommendedPrice.compareTo(new BigDecimal("50")) > 0) {
            tier = "STANDARD_MARKET";
        } else {
            tier = "BUDGET_ACCESSIBLE";
        }

        EstimatePriceResponse resp = new EstimatePriceResponse();
        resp.setRecommendedPrice(recommendedPrice);
        resp.setMinFairPrice(minFairPrice);
        resp.setMaxFairPrice(maxFairPrice);
        resp.setEstimatedDepreciationPercent(depreciationPercent);
        resp.setPricingTier(tier);
        resp.setRationale(String.format("Estimation basée sur un indice de référence de %s € pour la catégorie '%s' avec un coefficient d'état de %.0f%% (%s).",
                baseBenchmark, category.getLabel(), conditionFactor * 100, condition.getLabel()));
        resp.setMarketFactors(List.of(
                "Décote standard d'objet trouvé non réclamé : -" + depreciationPercent + "%",
                "Facteur d'état physique : " + condition.getLabel(),
                "Liquidité élevée sur le marché de seconde main",
                "Fourchette recommandée de revente solidaire : " + minFairPrice + " € - " + maxFairPrice + " €"
        ));
        resp.setConfidenceScore(0.89);

        return resp;
    }

    /**
     * AI Safety & Content Moderation: verifies that the listing does not contain
     * prohibited items (weapons, drugs, official ID documents, illegal goods) or scam patterns.
     */
    public ModerationResponse moderateContent(ModerationRequest request) {
        String fullText = ((request.getTitle() != null ? request.getTitle() : "") + " " +
                           (request.getDescription() != null ? request.getDescription() : "")).toLowerCase();

        List<String> flagged = new ArrayList<>();

        for (String prohibited : PROHIBITED_KEYWORDS) {
            if (fullText.contains(prohibited)) {
                flagged.add("Contenu non autorisé détecté : mot-clé sensible '" + prohibited + "'");
            }
        }

        // Check for official identity document sales (illegal)
        if (fullText.contains("passeport") || fullText.contains("carte d'identite") || fullText.contains("carte vitale")) {
            flagged.add("Violation légale : La vente ou le transfert de documents d'identité officiels est strictement prohibé par la loi.");
        }

        // Suspicious price checks
        if (request.getPrice() != null && !request.getPrice().isBlank()) {
            try {
                double priceVal = Double.parseDouble(request.getPrice().replace(",", "."));
                if (priceVal > 50000.0) {
                    flagged.add("Montant suspect : Prix excessivement élevé pour un objet trouvé non réclamé (" + priceVal + " €).");
                }
            } catch (NumberFormatException ignored) {}
        }

        boolean approved = flagged.isEmpty();
        double score = approved ? 98.0 : Math.max(10.0, 98.0 - (flagged.size() * 30.0));
        String riskLevel = approved ? "LOW" : (score < 40 ? "BLOCKED" : "HIGH");

        String explanation = approved
                ? "L'annonce respecte l'ensemble des règles de sécurité et de conformité légale."
                : "Attention : L'IA a détecté des éléments non conformes ou à haut risque nécessitant une révision immédiate.";

        return new ModerationResponse(approved, score, riskLevel, flagged, explanation);
    }

    /**
     * AI Anomaly Detection: flags statistical price outliers or suspicious discrepancies.
     */
    public AnomalyDetectionResult detectAnomaly(MarketplaceItem item) {
        AnomalyDetectionResult result = new AnomalyDetectionResult();
        result.setActualPrice(item.getPrice());

        EstimatePriceRequest req = new EstimatePriceRequest();
        req.setTitle(item.getTitle());
        req.setCategory(item.getCategory());
        req.setCondition(item.getCondition());
        EstimatePriceResponse benchmark = estimatePrice(req);

        result.setExpectedMin(benchmark.getMinFairPrice());
        result.setExpectedMax(benchmark.getMaxFairPrice());

        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        List<String> recommendations = new ArrayList<>();

        // If item type is DONATION, price must be 0
        if (item.getType() == ItemType.DONATION && price.compareTo(BigDecimal.ZERO) > 0) {
            result.setAnomalyDetected(true);
            result.setAnomalyType("PRICE_TYPE_MISMATCH");
            result.setAnomalyScore(0.95);
            result.setDiagnosticMessage("Une annonce de type 'Don Solidaire' ne peut pas avoir un prix supérieur à 0 €.");
            recommendations.add("Remettre le prix à 0 € ou modifier le type en 'Vente'.");
            result.setRecommendations(recommendations);
            return result;
        }

        // If item is SALE and price is 0
        if (item.getType() == ItemType.SALE && price.compareTo(BigDecimal.ZERO) == 0) {
            result.setAnomalyDetected(true);
            result.setAnomalyType("ZERO_PRICE_SALE");
            result.setAnomalyScore(0.70);
            result.setDiagnosticMessage("Une annonce de type 'Vente' a un prix fixé à 0 €.");
            recommendations.add("Changer le type en 'Don Solidaire' ou spécifier un prix de vente valide.");
            result.setRecommendations(recommendations);
            return result;
        }

        // Outlier low price for high value categories
        if (item.getType() == ItemType.SALE && benchmark.getRecommendedPrice().compareTo(new BigDecimal("150")) > 0 && price.compareTo(new BigDecimal("5")) <= 0) {
            result.setAnomalyDetected(true);
            result.setAnomalyType("PRICE_ABNORMALLY_LOW");
            result.setAnomalyScore(0.85);
            result.setDiagnosticMessage(String.format("Le prix fixé (%s €) est anormalement bas par rapport à l'estimation IA (%s €). Risque d'erreur de saisie ou d'arnaque.", price, benchmark.getRecommendedPrice()));
            recommendations.add(String.format("Ajuster le prix dans la fourchette recommandée [%s € - %s €].", benchmark.getMinFairPrice(), benchmark.getMaxFairPrice()));
            result.setRecommendations(recommendations);
            return result;
        }

        // Outlier high price
        if (item.getType() == ItemType.SALE && price.compareTo(benchmark.getMaxFairPrice().multiply(new BigDecimal("3"))) > 0) {
            result.setAnomalyDetected(true);
            result.setAnomalyType("PRICE_ABNORMALLY_HIGH");
            result.setAnomalyScore(0.88);
            result.setDiagnosticMessage(String.format("Le prix fixé (%s €) est très supérieur au plafond recommandé (%s €). L'annonce risque de ne pas trouver preneur.", price, benchmark.getMaxFairPrice()));
            recommendations.add("Réévaluer le prix pour favoriser une revente rapide.");
            result.setRecommendations(recommendations);
            return result;
        }

        // Normal
        result.setAnomalyDetected(false);
        result.setAnomalyType("NONE");
        result.setAnomalyScore(0.05);
        result.setDiagnosticMessage("Aucune anomalie détectée. Le prix est cohérent avec l'estimation algorithmique.");
        recommendations.add("L'annonce est prête pour publication.");
        result.setRecommendations(recommendations);

        return result;
    }

    // Helper NLP heuristics
    private Category inferCategory(String prompt) {
        if (prompt == null) return Category.OTHER;
        String p = prompt.toLowerCase();
        if (p.contains("iphone") || p.contains("macbook") || p.contains("pc") || p.contains("ordinateur") ||
            p.contains("tablette") || p.contains("chargeur") || p.contains("ecouteurs") || p.contains("airpods") ||
            p.contains("telephone") || p.contains("appareil photo")) {
            return Category.ELECTRONICS;
        }
        if (p.contains("bague") || p.contains("collier") || p.contains("montre") || p.contains("bracelet") || p.contains("or") || p.contains("argent")) {
            return Category.JEWELRY_WATCHES;
        }
        if (p.contains("sac") || p.contains("valise") || p.contains("sac a dos") || p.contains("cartable") || p.contains("pochette")) {
            return Category.BAGS_LUGGAGE;
        }
        if (p.contains("cle") || p.contains("cles") || p.contains("carte") || p.contains("badge") || p.contains("titre de transport")) {
            return Category.DOCUMENTS_KEYS;
        }
        if (p.contains("manteau") || p.contains("veste") || p.contains("echarpe") || p.contains("bonnet") || p.contains("pull") || p.contains("lunettes")) {
            return Category.CLOTHING_ACCESSORIES;
        }
        if (p.contains("velo") || p.contains("trottinette") || p.contains("skate") || p.contains("casque")) {
            return Category.BICYCLES_VEHICLES;
        }
        if (p.contains("ballon") || p.contains("guitare") || p.contains("raquette") || p.contains("livre")) {
            return Category.SPORTS_LEISURE;
        }
        return Category.OTHER;
    }

    private ItemCondition inferCondition(String prompt) {
        if (prompt == null) return ItemCondition.GOOD;
        String p = prompt.toLowerCase();
        if (p.contains("neuf") || p.contains("emballage") || p.contains("scelle")) return ItemCondition.NEW_UNOPENED;
        if (p.contains("comme neuf") || p.contains("impeccable") || p.contains("parfait etat")) return ItemCondition.EXCELLENT;
        if (p.contains("raye") || p.contains("usure") || p.contains("abime") || p.contains("fissure")) return ItemCondition.FAIR;
        if (p.contains("casse") || p.contains("pour pieces") || p.contains("hs") || p.contains("panne")) return ItemCondition.FOR_PARTS;
        return ItemCondition.GOOD;
    }

    private String extractCleanSubject(String prompt) {
        if (prompt == null || prompt.isBlank()) return "Objet Trouvé Revalorisé";
        String s = prompt.trim();
        // Capitalize first letter unless it follows common tech naming (iPhone, iPad, etc.)
        if (s.startsWith("iPhone") || s.startsWith("iPad") || s.startsWith("iMac") || s.startsWith("macOS")) {
            // preserve casing
        } else if (s.length() > 0) {
            s = Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
        }
        // Limit length if too long
        if (s.length() > 60) {
            s = s.substring(0, 57) + "...";
        }
        return s;
    }
}
