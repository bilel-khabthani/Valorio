package com.lostandfound.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public class EstimatePriceResponse {

    private BigDecimal recommendedPrice;
    private BigDecimal minFairPrice;
    private BigDecimal maxFairPrice;
    private BigDecimal estimatedDepreciationPercent;
    private String pricingTier;
    private String rationale;
    private List<String> marketFactors;
    private Double confidenceScore;

    public EstimatePriceResponse() {}

    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }

    public void setRecommendedPrice(BigDecimal recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }

    public BigDecimal getMinFairPrice() {
        return minFairPrice;
    }

    public void setMinFairPrice(BigDecimal minFairPrice) {
        this.minFairPrice = minFairPrice;
    }

    public BigDecimal getMaxFairPrice() {
        return maxFairPrice;
    }

    public void setMaxFairPrice(BigDecimal maxFairPrice) {
        this.maxFairPrice = maxFairPrice;
    }

    public BigDecimal getEstimatedDepreciationPercent() {
        return estimatedDepreciationPercent;
    }

    public void setEstimatedDepreciationPercent(BigDecimal estimatedDepreciationPercent) {
        this.estimatedDepreciationPercent = estimatedDepreciationPercent;
    }

    public String getPricingTier() {
        return pricingTier;
    }

    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public List<String> getMarketFactors() {
        return marketFactors;
    }

    public void setMarketFactors(List<String> marketFactors) {
        this.marketFactors = marketFactors;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
