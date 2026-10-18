package com.valorio.marketplace.audit;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class AuditLog {

    private final JdbcClient jdbc;

    public AuditLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void enregistrer(String evenement, String entite, Long entiteId, String details) {
        jdbc.sql("""
                        INSERT INTO audit_log (evenement, entite, entite_id, details)
                        VALUES (:evenement, :entite, :entiteId, :details)
                        """)
                .param("evenement", evenement)
                .param("entite", entite)
                .param("entiteId", entiteId)
                .param("details", details)
                .update();
    }
}
