package com.lostandfound.marketplace.dto;

import java.util.List;

public class ModerationResponse {

    private boolean approved;
    private Double safetyScore; // 0 to 100
    private String riskLevel; // LOW, MEDIUM, HIGH, BLOCKED
    private List<String> flaggedIssues;
    private String aiExplanation;

    public ModerationResponse() {}

    public ModerationResponse(boolean approved, Double safetyScore, String riskLevel, List<String> flaggedIssues, String aiExplanation) {
        this.approved = approved;
        this.safetyScore = safetyScore;
        this.riskLevel = riskLevel;
        this.flaggedIssues = flaggedIssues;
        this.aiExplanation = aiExplanation;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Double getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(Double safetyScore) {
        this.safetyScore = safetyScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getFlaggedIssues() {
        return flaggedIssues;
    }

    public void setFlaggedIssues(List<String> flaggedIssues) {
        this.flaggedIssues = flaggedIssues;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}
