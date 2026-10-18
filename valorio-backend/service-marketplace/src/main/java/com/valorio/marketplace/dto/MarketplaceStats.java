package com.valorio.marketplace.dto;

import java.util.Map;

public record MarketplaceStats(
        long totalItems,
        long availableItems,
        long soldItems,
        long pendingOffers,
        double averageSafetyScore,
        double aiAssistedPercentage,
        Map<String, Long> itemsByCategory,
        Map<String, Long> itemsByType
) {
}
