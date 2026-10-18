package com.valorio.objets.service;

import com.valorio.objets.dto.DashboardStats;
import com.valorio.objets.repo.DiagnosticRepository;
import com.valorio.objets.repo.HistoriqueRepository;
import com.valorio.objets.repo.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final StatsRepository stats;
    private final DiagnosticRepository diagnostics;
    private final HistoriqueRepository historique;

    public StatsService(StatsRepository stats, DiagnosticRepository diagnostics, HistoriqueRepository historique) {
        this.stats = stats;
        this.diagnostics = diagnostics;
        this.historique = historique;
    }

    public DashboardStats getStats() {
        return new DashboardStats(
                stats.compterObjets(),
                Math.round(stats.valeurTotaleEstimee()),
                stats.compterParStatut(),
                stats.compterParEtat(),
                diagnostics.compter(),
                historique.compter());
    }
}
