package com.valorio.auth.ai.model;

import java.util.List;

public record CodeReviewResponse(
        String summary,
        int overallScore,
        List<ReviewFinding> findings,
        List<String> recommendations
) {}
