package com.valorio.auth.ai.model;

public record QualityFinding(
        String id,
        String category,
        String severity, // LOW, MEDIUM, HIGH, CRITICAL
        String description,
        String evidence,
        String lineReference,
        String suggestedCorrection,
        String source, // STATIC_ANALYSIS ou AI_PREDICTED
        double confidenceLevel
) {}
