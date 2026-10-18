package com.valorio.objets.dto;

import java.util.Map;

public record DashboardStats(
        long totalObjets,
        long totalValeurEstimee,
        Map<String, Long> parStatut,
        Map<String, Long> parEtat,
        long totalDiagnostics,
        long totalEvenements
) {
}
