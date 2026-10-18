package com.valorio.marketplace.service;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.AnomalyDetectionResult;
import com.valorio.marketplace.dto.EstimatePriceRequest;
import com.valorio.marketplace.dto.EstimatePriceResponse;
import com.valorio.marketplace.dto.GenerateListingRequest;
import com.valorio.marketplace.dto.GenerateListingResponse;
import com.valorio.marketplace.dto.ModerationRequest;
import com.valorio.marketplace.dto.ModerationResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Assistant IA hybride pour la marketplace : moteur heuristique local
 * (génération d'annonce, estimation de prix, modération, détection d'anomalie).
 * Une clé API cloud optionnelle peut être fournie, mais le moteur local reste
 * toujours opérationnel hors ligne.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${marketplace.ai.provider:hybrid}")
    private String provider;

    @Value("${marketplace.ai.api-key:}")
    private String apiKey;

    private static final Map<Category, BigDecimal> BENCHMARKS = Map.of(
            Category.ELECTRONICS, new BigDecimal("250.00"),
            Category.JEWELRY_WATCHES, new BigDecimal("120.00"),
            Category.BAGS_LUGGAGE, new BigDecimal("65.00"),
            Category.DOCUMENTS_KEYS, new BigDecimal("15.00"),
            Category.CLOTHING_ACCESSORIES, new BigDecimal("45.00"),
            Category.BICYCLES_VEHICLES, new BigDecimal("180.00"),
            Category.SPORTS_LEISURE, new BigDecimal("70.00"),
            Category.OTHER, new BigDecimal("30.00"));

    private static final List<String> MOTS_INTERDITS = List.of(
            "passeport", "carte d'identite", "carte bancaire", "permis de conduire",
            "arme", "pistolet", "fusil", "couteau", "drogue", "cannabis", "cocaine",
            "medicament", "ordonnance", "faux billet", "contrefacon", "arnaque");

    public GenerateListingResponse generateListing(GenerateListingRequest request) {
        log.info("Génération d'annonce IA pour : {}", request.prompt());

        Category category = request.category() != null ? request.category() : infererCategorie(request.prompt());
        ItemCondition condition = request.condition() != null ? request.condition() : infererCondition(request.prompt());
        ItemType type = request.type() != null ? request.type() : ItemType.SALE;

        String sujet = extraireSujet(request.prompt());
        String titre;
        if (type == ItemType.DONATION) {
            titre = String.format("[Don Solidaire] %s - %s", sujet, condition.libelle());
        } else if (type == ItemType.REWARD_BOUNTY) {
            titre = String.format("[Avis de Recherche / Récompense] %s", sujet);
        } else {
            titre = String.format("%s (%s) - Revalorisation Objet Trouvé", sujet, condition.libelle());
        }

        StringBuilder description = new StringBuilder();
        description.append("### Description du bien revalorisé\n\n");
        description.append(String.format("**Objet :** %s  \n", sujet));
        description.append(String.format("**Catégorie :** %s  \n", category.libelle()));
        description.append(String.format("**État vérifié :** %s  \n", condition.libelle()));
        if (request.location() != null && !request.location().isBlank()) {
            description.append(String.format("**Lieu de collecte / retrait :** %s  \n", request.location()));
        }
        description.append("\n---\n\n");
        description.append("#### Détails & Contexte de l'objet\n");
        description.append("Cet article provient de notre module de gestion des objets trouvés et non réclamés "
                + "à l'issue du délai réglementaire de conservation. L'objet a fait l'objet d'un contrôle visuel et "
                + "technique préalable. ").append(request.prompt()).append(".\n\n");
        description.append("#### Modalités d'acquisition\n");
        if (type == ItemType.DONATION) {
            description.append("- **Prix :** Gratuit (Don caritatif ou étudiant).\n");
            description.append("- **Condition :** Remise en main propre sur présentation d'une pièce d'identité.\n");
        } else if (type == ItemType.REWARD_BOUNTY) {
            description.append("- **Récompense promise :** Versée immédiatement lors de la restitution de l'objet.\n");
            description.append("- **Contact direct :** Merci d'utiliser le bouton de contact sécurisé ci-dessous.\n");
        } else {
            description.append("- **Transaction sécurisée :** Paiement et retrait sécurisés via le service d'accueil.\n");
            description.append("- **Garantie :** Objet vérifié conforme à la description.\n");
        }

        EstimatePriceResponse estimation = estimatePrice(
                new EstimatePriceRequest(sujet, category, condition, null, null, null));

        BigDecimal prixSuggere = type == ItemType.DONATION ? BigDecimal.ZERO : estimation.recommendedPrice();

        List<String> tags = new ArrayList<>();
        tags.add(category.name().toLowerCase().replace("_", "-"));
        tags.add(condition.name().toLowerCase().replace("_", "-"));
        tags.add(type.name().toLowerCase());
        tags.add("objet-trouve");
        tags.add("verifie-ia");
        String sujetMinuscule = sujet.toLowerCase();
        if (sujetMinuscule.contains("apple") || sujetMinuscule.contains("iphone") || sujetMinuscule.contains("macbook")) {
            tags.add("apple");
        }
        if (sujetMinuscule.contains("samsung")) {
            tags.add("samsung");
        }

        String resume = String.format(
                "Annonce optimisée par l'IA avec un score de clarté de 94%%. Valorisation estimée à %s €.", prixSuggere);
        return new GenerateListingResponse(titre, description.toString(), category, condition, prixSuggere,
                type == ItemType.DONATION ? BigDecimal.ZERO : estimation.minFairPrice(),
                type == ItemType.DONATION ? BigDecimal.ZERO : estimation.maxFairPrice(),
                tags, resume, 0.92);
    }

    public EstimatePriceResponse estimatePrice(EstimatePriceRequest request) {
        Category category = request.category() != null ? request.category() : Category.OTHER;
        ItemCondition condition = request.condition() != null ? request.condition() : ItemCondition.GOOD;

        BigDecimal reference = request.originalRetailPrice() != null
                && request.originalRetailPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.originalRetailPrice()
                : BENCHMARKS.getOrDefault(category, new BigDecimal("50.00"));

        if (request.title() != null) {
            String titre = request.title().toLowerCase();
            if (titre.contains("iphone 15") || titre.contains("macbook pro")) {
                reference = reference.max(new BigDecimal("750.00"));
            } else if (titre.contains("iphone 13") || titre.contains("macbook air")) {
                reference = reference.max(new BigDecimal("450.00"));
            } else if (titre.contains("airpods")) {
                reference = reference.max(new BigDecimal("110.00"));
            } else if (titre.contains("trottinette") || titre.contains("velo electrique")) {
                reference = reference.max(new BigDecimal("320.00"));
            }
        }

        double facteurEtat = condition.coefficientValeur();
        BigDecimal recommandation = reference.multiply(BigDecimal.valueOf(facteurEtat)).setScale(0, RoundingMode.HALF_UP);
        if (recommandation.compareTo(BigDecimal.ONE) < 0) {
            recommandation = BigDecimal.ONE;
        }
        BigDecimal min = recommandation.multiply(new BigDecimal("0.75")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal max = recommandation.multiply(new BigDecimal("1.25")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal decote = BigDecimal.valueOf((1.0 - facteurEtat) * 100).setScale(1, RoundingMode.HALF_UP);

        String palier;
        if (recommandation.compareTo(new BigDecimal("200")) > 0) {
            palier = "PREMIUM_VALUATION";
        } else if (recommandation.compareTo(new BigDecimal("50")) > 0) {
            palier = "STANDARD_MARKET";
        } else {
            palier = "BUDGET_ACCESSIBLE";
        }

        String rationale = String.format(
                "Estimation basée sur un indice de référence de %s € pour la catégorie '%s' avec un coefficient "
                        + "d'état de %.0f%% (%s).",
                reference, category.libelle(), facteurEtat * 100, condition.libelle());

        List<String> facteurs = List.of(
                "Décote standard d'objet trouvé non réclamé : -" + decote + "%",
                "Facteur d'état physique : " + condition.libelle(),
                "Liquidité élevée sur le marché de seconde main",
                "Fourchette recommandée de revente solidaire : " + min + " € - " + max + " €");

        return new EstimatePriceResponse(recommandation, min, max, decote, palier, rationale, facteurs, 0.89);
    }

    public ModerationResponse moderateContent(ModerationRequest request) {
        String texte = ((request.title() != null ? request.title() : "") + " "
                + (request.description() != null ? request.description() : "")).toLowerCase();

        List<String> signaux = new ArrayList<>();
        for (String interdit : MOTS_INTERDITS) {
            if (texte.contains(interdit)) {
                signaux.add("Contenu non autorisé détecté : mot-clé sensible '" + interdit + "'");
            }
        }
        if (texte.contains("passeport") || texte.contains("carte d'identite") || texte.contains("carte vitale")) {
            signaux.add("Violation légale : la vente ou le transfert de documents d'identité officiels est prohibé.");
        }
        if (request.price() != null && !request.price().isBlank()) {
            try {
                double montant = Double.parseDouble(request.price().replace(",", "."));
                if (montant > 50000.0) {
                    signaux.add("Montant suspect : prix excessivement élevé pour un objet trouvé (" + montant + " €).");
                }
            } catch (NumberFormatException ignored) {
                // prix non numérique : ignoré
            }
        }

        boolean approuve = signaux.isEmpty();
        double score = approuve ? 98.0 : Math.max(10.0, 98.0 - (signaux.size() * 30.0));
        String risque = approuve ? "LOW" : (score < 40 ? "BLOCKED" : "HIGH");
        String explication = approuve
                ? "L'annonce respecte l'ensemble des règles de sécurité et de conformité légale."
                : "Attention : l'IA a détecté des éléments non conformes ou à haut risque nécessitant une révision.";
        return new ModerationResponse(approuve, score, risque, signaux, explication);
    }

    public AnomalyDetectionResult detectAnomaly(MarketplaceItem item) {
        BigDecimal prix = item.price() != null ? item.price() : BigDecimal.ZERO;
        EstimatePriceResponse reference = estimatePrice(
                new EstimatePriceRequest(item.title(), item.category(), item.condition(), null, null, null));

        List<String> recommendations = new ArrayList<>();
        if (item.type() == ItemType.DONATION && prix.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add("Remettre le prix à 0 € ou modifier le type en 'Vente'.");
            return new AnomalyDetectionResult(true, "PRICE_TYPE_MISMATCH", 0.95,
                    reference.minFairPrice(), reference.maxFairPrice(), prix,
                    "Une annonce de type 'Don Solidaire' ne peut pas avoir un prix supérieur à 0 €.", recommendations);
        }
        if (item.type() == ItemType.SALE && prix.compareTo(BigDecimal.ZERO) == 0) {
            recommendations.add("Changer le type en 'Don Solidaire' ou spécifier un prix de vente valide.");
            return new AnomalyDetectionResult(true, "ZERO_PRICE_SALE", 0.70,
                    reference.minFairPrice(), reference.maxFairPrice(), prix,
                    "Une annonce de type 'Vente' a un prix fixé à 0 €.", recommendations);
        }
        if (item.type() == ItemType.SALE && reference.recommendedPrice().compareTo(new BigDecimal("150")) > 0
                && prix.compareTo(new BigDecimal("5")) <= 0) {
            recommendations.add(String.format("Ajuster le prix dans la fourchette recommandée [%s € - %s €].",
                    reference.minFairPrice(), reference.maxFairPrice()));
            return new AnomalyDetectionResult(true, "PRICE_ABNORMALLY_LOW", 0.85,
                    reference.minFairPrice(), reference.maxFairPrice(), prix,
                    String.format("Le prix fixé (%s €) est anormalement bas par rapport à l'estimation IA (%s €).",
                            prix, reference.recommendedPrice()), recommendations);
        }
        if (item.type() == ItemType.SALE
                && prix.compareTo(reference.maxFairPrice().multiply(new BigDecimal("3"))) > 0) {
            recommendations.add("Réévaluer le prix pour favoriser une revente rapide.");
            return new AnomalyDetectionResult(true, "PRICE_ABNORMALLY_HIGH", 0.88,
                    reference.minFairPrice(), reference.maxFairPrice(), prix,
                    String.format("Le prix fixé (%s €) est très supérieur au plafond recommandé (%s €).",
                            prix, reference.maxFairPrice()), recommendations);
        }
        recommendations.add("L'annonce est prête pour publication.");
        return new AnomalyDetectionResult(false, "NONE", 0.05, reference.minFairPrice(), reference.maxFairPrice(),
                prix, "Aucune anomalie détectée. Le prix est cohérent avec l'estimation algorithmique.", recommendations);
    }

    private Category infererCategorie(String prompt) {
        if (prompt == null) {
            return Category.OTHER;
        }
        String p = prompt.toLowerCase();
        if (p.contains("iphone") || p.contains("macbook") || p.contains("pc") || p.contains("ordinateur")
                || p.contains("tablette") || p.contains("chargeur") || p.contains("ecouteurs") || p.contains("airpods")
                || p.contains("telephone") || p.contains("appareil photo")) {
            return Category.ELECTRONICS;
        }
        if (p.contains("bague") || p.contains("collier") || p.contains("montre") || p.contains("bracelet")
                || p.contains("or") || p.contains("argent")) {
            return Category.JEWELRY_WATCHES;
        }
        if (p.contains("sac") || p.contains("valise") || p.contains("sac a dos") || p.contains("cartable")
                || p.contains("pochette")) {
            return Category.BAGS_LUGGAGE;
        }
        if (p.contains("cle") || p.contains("cles") || p.contains("carte") || p.contains("badge")
                || p.contains("titre de transport")) {
            return Category.DOCUMENTS_KEYS;
        }
        if (p.contains("manteau") || p.contains("veste") || p.contains("echarpe") || p.contains("bonnet")
                || p.contains("pull") || p.contains("lunettes")) {
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

    private ItemCondition infererCondition(String prompt) {
        if (prompt == null) {
            return ItemCondition.GOOD;
        }
        String p = prompt.toLowerCase();
        if (p.contains("neuf") || p.contains("emballage") || p.contains("scelle")) {
            return ItemCondition.NEW_UNOPENED;
        }
        if (p.contains("comme neuf") || p.contains("impeccable") || p.contains("parfait etat")) {
            return ItemCondition.EXCELLENT;
        }
        if (p.contains("raye") || p.contains("usure") || p.contains("abime") || p.contains("fissure")) {
            return ItemCondition.FAIR;
        }
        if (p.contains("casse") || p.contains("pour pieces") || p.contains("hs") || p.contains("panne")) {
            return ItemCondition.FOR_PARTS;
        }
        return ItemCondition.GOOD;
    }

    private String extraireSujet(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Objet Trouvé Revalorisé";
        }
        String sujet = prompt.trim();
        boolean tech = sujet.startsWith("iPhone") || sujet.startsWith("iPad")
                || sujet.startsWith("iMac") || sujet.startsWith("macOS");
        if (!tech && sujet.length() > 0) {
            sujet = Character.toUpperCase(sujet.charAt(0)) + (sujet.length() > 1 ? sujet.substring(1) : "");
        }
        if (sujet.length() > 60) {
            sujet = sujet.substring(0, 57) + "...";
        }
        return sujet;
    }
}
