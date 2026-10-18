package com.valorio.marketplace.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MarketplaceOffer(
        Long id,
        MarketplaceItem item,
        String buyerName,
        String buyerEmail,
        String buyerPhone,
        BigDecimal offeredPrice,
        String message,
        OfferStatus status,
        OffsetDateTime createdAt
) {
}
