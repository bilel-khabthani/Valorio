package com.valorio.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstimatePriceResponse(
        BigDecimal recommendedPrice,
        BigDecimal minFairPrice,
        BigDecimal maxFairPrice,
        BigDecimal estimatedDepreciationPercent,
        String pricingTier,
        String rationale,
        List<String> marketFactors,
        Double confidenceScore
) {
}
