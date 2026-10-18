package com.valorio.marketplace.service;

import com.valorio.marketplace.audit.AuditLog;
import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.EstimatePriceRequest;
import com.valorio.marketplace.dto.EstimatePriceResponse;
import com.valorio.marketplace.dto.ItemRequest;
import com.valorio.marketplace.dto.MarketplaceStats;
import com.valorio.marketplace.dto.ModerationRequest;
import com.valorio.marketplace.dto.ModerationResponse;
import com.valorio.marketplace.external.UtilisateurPort;
import com.valorio.marketplace.repo.MarketplaceItemRepository;
import com.valorio.marketplace.repo.MarketplaceOfferRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketplaceItemService {

    private final MarketplaceItemRepository articles;
    private final MarketplaceOfferRepository offres;
    private final AiService ia;
    private final UtilisateurPort utilisateurs;
    private final ObjetSynchronisation synchronisation;
    private final AuditLog audit;

    public MarketplaceItemService(MarketplaceItemRepository articles,
                                  MarketplaceOfferRepository offres,
                                  AiService ia,
                                  UtilisateurPort utilisateurs,
                                  ObjetSynchronisation synchronisation,
                                  AuditLog audit) {
        this.articles = articles;
        this.offres = offres;
        this.ia = ia;
        this.utilisateurs = utilisateurs;
        this.synchronisation = synchronisation;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<MarketplaceItem> rechercher(Category category, ItemType type, ItemStatus status,
                                            BigDecimal minPrice, BigDecimal maxPrice, String q) {
        return articles.rechercher(category, type, status, minPrice, maxPrice, q);
    }

    @Transactional(readOnly = true)
    public MarketplaceItem detail(long id) {
        return articles.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + id));
    }

    @Transactional
    public MarketplaceItem consulter(long id) {
        detail(id);
        articles.incrementerVues(id);
        return detail(id);
    }

    @Transactional
    public MarketplaceItem creer(ItemRequest request) {
        validerVendeur(request.sellerId());

        ModerationResponse moderation = ia.moderateContent(new ModerationRequest(
                request.title(), request.description(), request.category(),
                request.price() != null ? request.price().toString() : "0"));
        EstimatePriceResponse estimation = ia.estimatePrice(new EstimatePriceRequest(
                request.title(), request.category(), request.condition(), null, null, null));

        String fairness = evaluerEquite(request, estimation);

        BigDecimal prix = request.price() != null ? request.price() : BigDecimal.ZERO;
        MarketplaceItem article = new MarketplaceItem(
                null,
                request.title(),
                request.description(),
                request.category(),
                request.condition(),
                request.type(),
                request.status() != null ? request.status() : ItemStatus.AVAILABLE,
                prix,
                estimation.recommendedPrice(),
                fairness,
                moderation.safetyScore(),
                moderation.aiExplanation() + (moderation.flaggedIssues().isEmpty()
                        ? "" : " Drapeaux : " + String.join(", ", moderation.flaggedIssues())),
                request.originalObjectId(),
                request.location(),
                request.sellerId(),
                request.sellerName(),
                request.sellerEmail(),
                request.sellerPhone(),
                request.imageUrl(),
                request.tags(),
                request.unclaimedDays() != null ? request.unclaimedDays() : 90,
                0,
                null,
                null);

        long id = articles.creer(article);
        audit.enregistrer("MARKETPLACE_ARTICLE_CREE", "marketplace_item", id,
                "categorie=" + request.category() + ", type=" + request.type());
        return detail(id);
    }

    @Transactional
    public MarketplaceItem modifier(long id, ItemRequest request) {
        MarketplaceItem existant = detail(id);
        MarketplaceItem misAJour = new MarketplaceItem(
                existant.id(),
                request.title(),
                request.description(),
                request.category(),
                request.condition(),
                request.type(),
                request.status() != null ? request.status() : existant.status(),
                request.price() != null ? request.price() : existant.price(),
                existant.aiEstimatedPrice(),
                existant.aiPriceFairness(),
                existant.aiVerificationScore(),
                existant.aiModerationNotes(),
                existant.originalObjectId(),
                request.location(),
                existant.sellerId(),
                request.sellerName(),
                request.sellerEmail(),
                request.sellerPhone(),
                request.imageUrl(),
                request.tags(),
                existant.unclaimedDays(),
                existant.viewsCount(),
                existant.createdAt(),
                null);
        articles.maj(misAJour);
        audit.enregistrer("MARKETPLACE_ARTICLE_MODIFIE", "marketplace_item", id, "titre=" + request.title());
        return detail(id);
    }

    @Transactional
    public MarketplaceItem changerStatut(long id, ItemStatus status) {
        MarketplaceItem existant = detail(id);
        articles.majStatut(id, status);
        if (status == ItemStatus.SOLD) {
            synchronisation.signalerVente(existant.withStatus(status));
        }
        audit.enregistrer("MARKETPLACE_STATUT_MODIFIE", "marketplace_item", id, status.name());
        return detail(id);
    }

    @Transactional
    public void supprimer(long id) {
        MarketplaceItem existant = detail(id);
        articles.supprimer(id);
        audit.enregistrer("MARKETPLACE_ARTICLE_SUPPRIME", "marketplace_item", id, existant.title());
    }

    @Transactional(readOnly = true)
    public MarketplaceStats statistiques() {
        double moyenne = Math.round(articles.moyenneScoreIA() * 10.0) / 10.0;
        return new MarketplaceStats(
                articles.compter(),
                articles.compterParStatut(ItemStatus.AVAILABLE),
                articles.compterParStatut(ItemStatus.SOLD),
                offres.compterParStatut(com.valorio.marketplace.domain.OfferStatus.PENDING),
                moyenne,
                98.5,
                articles.compterParCategorie(),
                articles.compterParType());
    }

    private void validerVendeur(Long sellerId) {
        if (sellerId != null && utilisateurs.findById(sellerId).isEmpty()) {
            throw new NoSuchElementException("Utilisateur " + sellerId + " introuvable");
        }
    }

    private String evaluerEquite(ItemRequest request, EstimatePriceResponse estimation) {
        if (request.type() == ItemType.DONATION) {
            return "SOLIDARITY_FREE";
        }
        if (request.price() == null) {
            return "FAIR_MARKET";
        }
        if (request.price().compareTo(estimation.minFairPrice()) <= 0) {
            return "EXCELLENT_DEAL";
        }
        if (request.price().compareTo(estimation.maxFairPrice()) <= 0) {
            return "FAIR_MARKET";
        }
        return "PREMIUM_PRICED";
    }
}
