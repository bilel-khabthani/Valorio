package com.lostandfound.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public class AnomalyDetectionResult {

    private boolean anomalyDetected;
    private String anomalyType; // PRICE_OUTLIER, SUSPICIOUS_DESCRIPTION, CATEGORY_MISMATCH, NONE
    private Double anomalyScore; // 0 to 1
    private BigDecimal expectedMin;
    private BigDecimal expectedMax;
    private BigDecimal actualPrice;
    private String diagnosticMessage;
    private List<String> recommendations;

    public AnomalyDetectionResult() {}

    public boolean isAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public Double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public BigDecimal getExpectedMin() {
        return expectedMin;
    }

    public void setExpectedMin(BigDecimal expectedMin) {
        this.expectedMin = expectedMin;
    }

    public BigDecimal getExpectedMax() {
        return expectedMax;
    }

    public void setExpectedMax(BigDecimal expectedMax) {
        this.expectedMax = expectedMax;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }

    public void setDiagnosticMessage(String diagnosticMessage) {
        this.diagnosticMessage = diagnosticMessage;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
