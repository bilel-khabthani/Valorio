package com.valorio.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnomalyDetectionResult(
        boolean anomalyDetected,
        String anomalyType,
        Double anomalyScore,
        BigDecimal expectedMin,
        BigDecimal expectedMax,
        BigDecimal actualPrice,
        String diagnosticMessage,
        List<String> recommendations
) {
}
