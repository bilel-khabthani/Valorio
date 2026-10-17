package com.lostandfound.marketplace.service;

import com.lostandfound.marketplace.dto.EstimatePriceRequest;
import com.lostandfound.marketplace.dto.EstimatePriceResponse;
import com.lostandfound.marketplace.dto.MarketplaceStatsResponse;
import com.lostandfound.marketplace.dto.ModerationRequest;
import com.lostandfound.marketplace.dto.ModerationResponse;
import com.lostandfound.marketplace.model.*;
import com.lostandfound.marketplace.repository.MarketplaceItemRepository;
import com.lostandfound.marketplace.repository.MarketplaceOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class MarketplaceItemService {

    private final MarketplaceItemRepository itemRepository;
    private final MarketplaceOfferRepository offerRepository;
    private final AiService aiService;

    public MarketplaceItemService(MarketplaceItemRepository itemRepository,
                                  MarketplaceOfferRepository offerRepository,
                                  AiService aiService) {
        this.itemRepository = itemRepository;
        this.offerRepository = offerRepository;
        this.aiService = aiService;
    }

    @Transactional(readOnly = true)
    public List<MarketplaceItem> searchItems(Category category, ItemType type, ItemStatus status,
                                             BigDecimal minPrice, BigDecimal maxPrice, String search) {
        List<MarketplaceItem> all = itemRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        if (category == null && type == null && status == null && minPrice == null && maxPrice == null && (search == null || search.isBlank())) {
            return all;
        }

        return all.stream()
                .filter(i -> category == null || i.getCategory() == category)
                .filter(i -> type == null || i.getType() == type)
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> minPrice == null || (i.getPrice() != null && i.getPrice().compareTo(minPrice) >= 0))
                .filter(i -> maxPrice == null || (i.getPrice() != null && i.getPrice().compareTo(maxPrice) <= 0))
                .filter(i -> {
                    if (search == null || search.isBlank()) return true;
                    String s = search.toLowerCase();
                    return (i.getTitle() != null && i.getTitle().toLowerCase().contains(s)) ||
                           (i.getDescription() != null && i.getDescription().toLowerCase().contains(s)) ||
                           (i.getTags() != null && i.getTags().toLowerCase().contains(s)) ||
                           (i.getLocation() != null && i.getLocation().toLowerCase().contains(s)) ||
                           (i.getSellerName() != null && i.getSellerName().toLowerCase().contains(s));
                })
                .toList();
    }

    public MarketplaceItem getItemById(Long id) {
        MarketplaceItem item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + id));
        item.setViewsCount((item.getViewsCount() == null ? 0 : item.getViewsCount()) + 1);
        return itemRepository.save(item);
    }

    public MarketplaceItem createItem(MarketplaceItem item) {
        // AI verification & safety moderation
        ModerationRequest modReq = new ModerationRequest(
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice() != null ? item.getPrice().toString() : "0"
        );
        ModerationResponse modRes = aiService.moderateContent(modReq);
        item.setAiVerificationScore(modRes.getSafetyScore());
        item.setAiModerationNotes(modRes.getAiExplanation() + (modRes.getFlaggedIssues().isEmpty() ? "" : " Drapeaux : " + String.join(", ", modRes.getFlaggedIssues())));

        // AI price estimation and fairness badge
        if (item.getAiEstimatedPrice() == null) {
            EstimatePriceRequest priceReq = new EstimatePriceRequest();
            priceReq.setTitle(item.getTitle());
            priceReq.setCategory(item.getCategory());
            priceReq.setCondition(item.getCondition());
            EstimatePriceResponse priceRes = aiService.estimatePrice(priceReq);
            item.setAiEstimatedPrice(priceRes.getRecommendedPrice());

            if (item.getType() == ItemType.DONATION) {
                item.setAiPriceFairness("SOLIDARITY_FREE");
            } else if (item.getPrice() != null && item.getPrice().compareTo(priceRes.getMinFairPrice()) <= 0) {
                item.setAiPriceFairness("EXCELLENT_DEAL");
            } else if (item.getPrice() != null && item.getPrice().compareTo(priceRes.getMaxFairPrice()) <= 0) {
                item.setAiPriceFairness("FAIR_MARKET");
            } else {
                item.setAiPriceFairness("PREMIUM_PRICED");
            }
        }

        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.AVAILABLE);
        }

        return itemRepository.save(item);
    }

    public MarketplaceItem updateItem(Long id, MarketplaceItem updated) {
        MarketplaceItem existing = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + id));

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setCondition(updated.getCondition());
        existing.setType(updated.getType());
        existing.setPrice(updated.getPrice());
        existing.setLocation(updated.getLocation());
        existing.setImageUrl(updated.getImageUrl());
        existing.setTags(updated.getTags());
        existing.setStatus(updated.getStatus());
        existing.setSellerName(updated.getSellerName());
        existing.setSellerEmail(updated.getSellerEmail());
        existing.setSellerPhone(updated.getSellerPhone());

        return itemRepository.save(existing);
    }

    public MarketplaceItem updateStatus(Long id, ItemStatus status) {
        MarketplaceItem existing = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Objet introuvable avec l'ID : " + id));
        existing.setStatus(status);
        return itemRepository.save(existing);
    }

    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new NoSuchElementException("Objet introuvable avec l'ID : " + id);
        }
        itemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MarketplaceStatsResponse getStatistics() {
        MarketplaceStatsResponse stats = new MarketplaceStatsResponse();
        stats.setTotalItems(itemRepository.count());
        stats.setAvailableItems(itemRepository.countByStatus(ItemStatus.AVAILABLE));
        stats.setSoldItems(itemRepository.countByStatus(ItemStatus.SOLD));
        stats.setPendingOffers(offerRepository.countByStatus(OfferStatus.PENDING));

        List<MarketplaceItem> all = itemRepository.findAll();
        double avgScore = all.stream()
                .filter(i -> i.getAiVerificationScore() != null)
                .mapToDouble(MarketplaceItem::getAiVerificationScore)
                .average()
                .orElse(95.0);
        stats.setAverageSafetyScore(Math.round(avgScore * 10.0) / 10.0);
        stats.setAiAssistedPercentage(98.5);

        Map<String, Long> catMap = new LinkedHashMap<>();
        for (Category cat : Category.values()) {
            long count = all.stream().filter(i -> i.getCategory() == cat).count();
            catMap.put(cat.name(), count);
        }
        stats.setItemsByCategory(catMap);

        Map<String, Long> typeMap = new LinkedHashMap<>();
        for (ItemType t : ItemType.values()) {
            long count = all.stream().filter(i -> i.getType() == t).count();
            typeMap.put(t.name(), count);
        }
        stats.setItemsByType(typeMap);

        return stats;
    }
}
