package com.valorio.objets.repo;

import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StatsRepository {

    private final JdbcClient jdbc;

    public StatsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long compterObjets() {
        return jdbc.sql("SELECT COUNT(*) FROM objet").query(Long.class).single();
    }

    public Double valeurTotaleEstimee() {
        Double valeur = jdbc.sql("SELECT COALESCE(SUM(valeur_estimee), 0) FROM objet")
                .query(Double.class)
                .single();
        return valeur == null ? 0.0 : valeur;
    }

    public Map<String, Long> compterParStatut() {
        return jdbc.sql("SELECT statut, COUNT(*) AS total FROM objet GROUP BY statut")
                .query((rs, i) -> Map.entry(rs.getString("statut"), rs.getLong("total")))
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public Map<String, Long> compterParEtat() {
        return jdbc.sql("SELECT etat, COUNT(*) AS total FROM objet GROUP BY etat")
                .query((rs, i) -> Map.entry(rs.getString("etat"), rs.getLong("total")))
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }
}
