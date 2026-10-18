package com.valorio.marketplace.dto;

import java.util.List;

public record ModerationResponse(
        boolean approved,
        Double safetyScore,
        String riskLevel,
        List<String> flaggedIssues,
        String aiExplanation
) {
}
