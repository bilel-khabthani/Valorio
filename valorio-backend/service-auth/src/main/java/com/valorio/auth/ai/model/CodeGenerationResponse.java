package com.valorio.auth.ai.model;

import java.util.List;

public record CodeGenerationResponse(
        String code,
        String language,
        String explanation,
        List<String> assumptions,
        List<String> potentialRisks,
        List<String> suggestedImprovements
) {}
