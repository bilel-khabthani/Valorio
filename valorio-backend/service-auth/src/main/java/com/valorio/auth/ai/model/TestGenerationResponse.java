package com.valorio.auth.ai.model;

import java.util.List;

public record TestGenerationResponse(
        String generatedTests,
        String framework,
        String explanation,
        List<String> testCasesCovered,
        List<String> edgeCases,
        List<String> suggestions
) {}
