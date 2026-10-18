package com.valorio.marketplace.service;

import com.valorio.marketplace.audit.AuditLog;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.domain.MarketplaceOffer;
import com.valorio.marketplace.domain.OfferStatus;
import com.valorio.marketplace.dto.OfferRequest;
import com.valorio.marketplace.repo.MarketplaceItemRepository;
import com.valorio.marketplace.repo.MarketplaceOfferRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketplaceOfferService {

    private final MarketplaceOfferRepository offres;
    private final MarketplaceItemRepository articles;
    private final ObjetSynchronisation synchronisation;
    private final AuditLog audit;

    public MarketplaceOfferService(MarketplaceOfferRepository offres,
                                   MarketplaceItemRepository articles,
                                   ObjetSynchronisation synchronisation,
                                   AuditLog audit) {
        this.offres = offres;
        this.articles = articles;
        this.synchronisation = synchronisation;
        this.audit = audit;
    }

    @Transactional
    public MarketplaceOffer creer(long itemId, OfferRequest request) {
        MarketplaceItem article = articles.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + itemId));
        long id = offres.creer(itemId, request.buyerName(), request.buyerEmail(), request.buyerPhone(),
                request.offeredPrice(), request.message(), OfferStatus.PENDING);
        audit.enregistrer("MARKETPLACE_OFFRE_CREEE", "marketplace_offer", id,
                "item=" + article.id() + ", acheteur=" + request.buyerEmail());
        return offres.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<MarketplaceOffer> offresParObjet(long itemId) {
        return offres.findByItemId(itemId);
    }

    @Transactional(readOnly = true)
    public List<MarketplaceOffer> toutesLesOffres() {
        return offres.findAll();
    }

    @Transactional
    public MarketplaceOffer changerStatut(long offerId, OfferStatus status) {
        MarketplaceOffer offre = offres.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offre introuvable avec l'ID : " + offerId));
        offres.majStatut(offerId, status);

        MarketplaceItem article = offre.item();
        if (status == OfferStatus.ACCEPTED) {
            articles.majStatut(article.id(), ItemStatus.RESERVED);
        } else if (status == OfferStatus.COMPLETED) {
            articles.majStatut(article.id(), ItemStatus.SOLD);
            synchronisation.signalerVente(article.withStatus(ItemStatus.SOLD));
        } else if (status == OfferStatus.REJECTED && article.status() == ItemStatus.RESERVED) {
            articles.majStatut(article.id(), ItemStatus.AVAILABLE);
        }

        audit.enregistrer("MARKETPLACE_OFFRE_STATUT_MODIFIE", "marketplace_offer", offerId, status.name());
        return offres.findById(offerId).orElseThrow();
    }

    @Transactional
    public void supprimer(long offerId) {
        if (!offres.existe(offerId)) {
            throw new NoSuchElementException("Offre introuvable avec l'ID : " + offerId);
        }
        offres.supprimer(offerId);
        audit.enregistrer("MARKETPLACE_OFFRE_SUPPRIMEE", "marketplace_offer", offerId, null);
    }
}
