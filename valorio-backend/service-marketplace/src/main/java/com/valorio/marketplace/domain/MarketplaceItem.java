package com.valorio.marketplace.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MarketplaceItem(
        Long id,
        String title,
        String description,
        Category category,
        ItemCondition condition,
        ItemType type,
        ItemStatus status,
        BigDecimal price,
        BigDecimal aiEstimatedPrice,
        String aiPriceFairness,
        Double aiVerificationScore,
        String aiModerationNotes,
        String originalObjectId,
        String location,
        Long sellerId,
        String sellerName,
        String sellerEmail,
        String sellerPhone,
        String imageUrl,
        String tags,
        Integer unclaimedDays,
        Integer viewsCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public MarketplaceItem withId(Long nouvelId) {
        return new MarketplaceItem(nouvelId, title, description, category, condition, type, status, price,
                aiEstimatedPrice, aiPriceFairness, aiVerificationScore, aiModerationNotes, originalObjectId,
                location, sellerId, sellerName, sellerEmail, sellerPhone, imageUrl, tags, unclaimedDays,
                viewsCount, createdAt, updatedAt);
    }

    public MarketplaceItem withStatus(ItemStatus nouveauStatut) {
        return new MarketplaceItem(id, title, description, category, condition, type, nouveauStatut, price,
                aiEstimatedPrice, aiPriceFairness, aiVerificationScore, aiModerationNotes, originalObjectId,
                location, sellerId, sellerName, sellerEmail, sellerPhone, imageUrl, tags, unclaimedDays,
                viewsCount, createdAt, updatedAt);
    }
}
