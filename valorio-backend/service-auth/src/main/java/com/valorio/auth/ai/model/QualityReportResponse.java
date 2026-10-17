package com.valorio.auth.ai.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record QualityReportResponse(
        String reportId,
        OffsetDateTime timestamp,
        String summary,
        String status, // PASSED, WARNING, FAILED
        Map<String, Integer> metrics,
        List<QualityFinding> findings
) {}
