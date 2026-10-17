package com.lostandfound.marketplace.dto;

import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemCondition;
import java.math.BigDecimal;
import java.util.List;

public class GenerateListingResponse {

    private String title;
    private String description;
    private Category suggestedCategory;
    private ItemCondition suggestedCondition;
    private BigDecimal suggestedPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> tags;
    private String aiSummary;
    private Double confidenceScore;

    public GenerateListingResponse() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(Category suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public ItemCondition getSuggestedCondition() {
        return suggestedCondition;
    }

    public void setSuggestedCondition(ItemCondition suggestedCondition) {
        this.suggestedCondition = suggestedCondition;
    }

    public BigDecimal getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSuggestedPrice(BigDecimal suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
