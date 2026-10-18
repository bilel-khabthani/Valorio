package com.valorio.marketplace.dto;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import java.math.BigDecimal;
import java.util.List;

public record GenerateListingResponse(
        String title,
        String description,
        Category suggestedCategory,
        ItemCondition suggestedCondition,
        BigDecimal suggestedPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> tags,
        String aiSummary,
        Double confidenceScore
) {
}
