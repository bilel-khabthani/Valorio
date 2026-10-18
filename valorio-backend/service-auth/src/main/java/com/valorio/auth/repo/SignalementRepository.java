package com.valorio.auth.repo;

import com.valorio.auth.domain.CibleSignalement;
import com.valorio.auth.domain.Signalement;
import com.valorio.auth.domain.StatutSignalement;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SignalementRepository {

    private static final String SELECT = """
            SELECT id, signaleur_id, cible_type, cible_id, motif, description, statut,
                   date_creation, date_traitement, traite_par, note_traitement
            FROM auth.signalement
            """;

    private static final RowMapper<Signalement> MAPPER = (rs, i) -> new Signalement(
            rs.getLong("id"),
            rs.getObject("signaleur_id", Long.class),
            CibleSignalement.depuis(rs.getString("cible_type")),
            rs.getObject("cible_id", Long.class),
            rs.getString("motif"),
            rs.getString("description"),
            StatutSignalement.valueOf(rs.getString("statut")),
            rs.getObject("date_creation", OffsetDateTime.class),
            rs.getObject("date_traitement", OffsetDateTime.class),
            rs.getObject("traite_par", Long.class),
            rs.getString("note_traitement"));

    private final JdbcClient jdbc;

    public SignalementRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creer(long signaleurId, CibleSignalement cibleType, Long cibleId, String motif, String description) {
        return jdbc.sql("""
                        INSERT INTO auth.signalement (signaleur_id, cible_type, cible_id, motif, description, statut)
                        VALUES (:signaleurId, :cibleType, :cibleId, :motif, :description, 'OUVERT')
                        RETURNING id
                        """)
                .param("signaleurId", signaleurId)
                .param("cibleType", cibleType.name())
                .param("cibleId", cibleId)
                .param("motif", motif)
                .param("description", description)
                .query(Long.class)
                .single();
    }

    public Optional<Signalement> findById(long id) {
        return jdbc.sql(SELECT + " WHERE id = :id")
                .param("id", id)
                .query(MAPPER)
                .optional();
    }

    public List<Signalement> lister(StatutSignalement statut, CibleSignalement cibleType) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (statut != null) {
            sql.append(" AND statut = :statut");
            params.put("statut", statut.name());
        }
        if (cibleType != null) {
            sql.append(" AND cible_type = :cibleType");
            params.put("cibleType", cibleType.name());
        }
        sql.append(" ORDER BY date_creation DESC");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query(MAPPER).list();
    }

    public void traiter(long id, StatutSignalement statut, Long traitePar, String note) {
        jdbc.sql("""
                        UPDATE auth.signalement
                        SET statut = :statut, traite_par = :traitePar, note_traitement = :note,
                            date_traitement = now()
                        WHERE id = :id
                        """)
                .param("statut", statut.name())
                .param("traitePar", traitePar)
                .param("note", note)
                .param("id", id)
                .update();
    }
}
