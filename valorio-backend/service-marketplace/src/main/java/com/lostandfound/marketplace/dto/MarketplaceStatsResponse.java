package com.lostandfound.marketplace.dto;

import java.util.Map;

public class MarketplaceStatsResponse {

    private long totalItems;
    private long availableItems;
    private long soldItems;
    private long pendingOffers;
    private double averageSafetyScore;
    private double aiAssistedPercentage;
    private Map<String, Long> itemsByCategory;
    private Map<String, Long> itemsByType;

    public MarketplaceStatsResponse() {}

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getAvailableItems() {
        return availableItems;
    }

    public void setAvailableItems(long availableItems) {
        this.availableItems = availableItems;
    }

    public long getSoldItems() {
        return soldItems;
    }

    public void setSoldItems(long soldItems) {
        this.soldItems = soldItems;
    }

    public long getPendingOffers() {
        return pendingOffers;
    }

    public void setPendingOffers(long pendingOffers) {
        this.pendingOffers = pendingOffers;
    }

    public double getAverageSafetyScore() {
        return averageSafetyScore;
    }

    public void setAverageSafetyScore(double averageSafetyScore) {
        this.averageSafetyScore = averageSafetyScore;
    }

    public double getAiAssistedPercentage() {
        return aiAssistedPercentage;
    }

    public void setAiAssistedPercentage(double aiAssistedPercentage) {
        this.aiAssistedPercentage = aiAssistedPercentage;
    }

    public Map<String, Long> getItemsByCategory() {
        return itemsByCategory;
    }

    public void setItemsByCategory(Map<String, Long> itemsByCategory) {
        this.itemsByCategory = itemsByCategory;
    }

    public Map<String, Long> getItemsByType() {
        return itemsByType;
    }

    public void setItemsByType(Map<String, Long> itemsByType) {
        this.itemsByType = itemsByType;
    }
}
