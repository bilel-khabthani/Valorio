package com.lostandfound.marketplace.service;

import com.lostandfound.marketplace.model.ItemStatus;
import com.lostandfound.marketplace.model.MarketplaceItem;
import com.lostandfound.marketplace.model.MarketplaceOffer;
import com.lostandfound.marketplace.model.OfferStatus;
import com.lostandfound.marketplace.repository.MarketplaceItemRepository;
import com.lostandfound.marketplace.repository.MarketplaceOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MarketplaceOfferService {

    private final MarketplaceOfferRepository offerRepository;
    private final MarketplaceItemRepository itemRepository;

    public MarketplaceOfferService(MarketplaceOfferRepository offerRepository,
                                   MarketplaceItemRepository itemRepository) {
        this.offerRepository = offerRepository;
        this.itemRepository = itemRepository;
    }

    public MarketplaceOffer createOffer(Long itemId, MarketplaceOffer offer) {
        MarketplaceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + itemId));
        offer.setItem(item);
        offer.setStatus(OfferStatus.PENDING);
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<MarketplaceOffer> getOffersByItem(Long itemId) {
        return offerRepository.findByItemIdOrderByCreatedAtDesc(itemId);
    }

    @Transactional(readOnly = true)
    public List<MarketplaceOffer> getAllOffers() {
        return offerRepository.findAllByOrderByCreatedAtDesc();
    }

    public MarketplaceOffer updateOfferStatus(Long offerId, OfferStatus status) {
        MarketplaceOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offre introuvable avec l'ID : " + offerId));

        offer.setStatus(status);

        // Synchronize item status with offer decision
        MarketplaceItem item = offer.getItem();
        if (status == OfferStatus.ACCEPTED) {
            item.setStatus(ItemStatus.RESERVED);
            itemRepository.save(item);
        } else if (status == OfferStatus.COMPLETED) {
            item.setStatus(ItemStatus.SOLD);
            itemRepository.save(item);
        } else if (status == OfferStatus.REJECTED && item.getStatus() == ItemStatus.RESERVED) {
            item.setStatus(ItemStatus.AVAILABLE);
            itemRepository.save(item);
        }

        return offerRepository.save(offer);
    }

    public void deleteOffer(Long offerId) {
        if (!offerRepository.existsById(offerId)) {
            throw new NoSuchElementException("Offre introuvable avec l'ID : " + offerId);
        }
        offerRepository.deleteById(offerId);
    }
}
