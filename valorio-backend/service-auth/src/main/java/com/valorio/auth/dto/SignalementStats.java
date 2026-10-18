package com.valorio.auth.dto;

import java.util.Map;

public record SignalementStats(
        long total,
        long ouverts,
        long enCours,
        long traites,
        long rejetes,
        java.util.Map<String, Long> parCible
) {
}
