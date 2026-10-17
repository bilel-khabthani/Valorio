package com.valorio.auth.ai.model;

public record ReviewFinding(
        String id,
        String category,
        String severity,
        String description,
        String evidence,
        String lineReference,
        String suggestedCorrection,
        String source,
        double confidenceLevel
) {}
