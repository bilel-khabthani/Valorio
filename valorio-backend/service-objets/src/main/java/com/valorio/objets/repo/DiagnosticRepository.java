package com.valorio.objets.repo;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.repo.ObjetRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DiagnosticRepository {

    private static final String SELECT_DIAGNOSTIC =
            "SELECT id, objet_id, panne_declaree, problemes, \"analyse\", diagnostic, date FROM diagnostic_objet";

    private static final RowMapper<DiagnosticObjet> DIAGNOSTIC_MAPPER = (rs, i) -> new DiagnosticObjet(
            rs.getLong("id"),
            rs.getLong("objet_id"),
            rs.getString("panne_declaree"),
            ObjetRepository.versMatieres(rs.getString("problemes")),
            rs.getString("analyse"),
            rs.getString("diagnostic"),
            rs.getObject("date", OffsetDateTime.class));

    private final JdbcClient jdbc;

    public DiagnosticRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creer(long objetId, String panneDeclaree, List<String> problemes, String analyse, String diagnostic) {
        return jdbc.sql("""
                        INSERT INTO diagnostic_objet (objet_id, panne_declaree, problemes, "analyse", diagnostic)
                        VALUES (:objetId, :panneDeclaree, :problemes, :analyse, :diagnostic)
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("panneDeclaree", panneDeclaree)
                .param("problemes", ObjetRepository.versCsv(problemes))
                .param("analyse", analyse)
                .param("diagnostic", diagnostic)
                .query(Long.class)
                .single();
    }

    public List<DiagnosticObjet> findDiagnostics(long objetId) {
        return jdbc.sql(SELECT_DIAGNOSTIC + " WHERE objet_id = :objetId ORDER BY date DESC, id DESC")
                .param("objetId", objetId)
                .query(DIAGNOSTIC_MAPPER)
                .list();
    }

    public List<DiagnosticObjet> findDerniersDiagnostics(long limite) {
        return jdbc.sql(SELECT_DIAGNOSTIC + " ORDER BY date DESC LIMIT :limite")
                .param("limite", limite)
                .query(DIAGNOSTIC_MAPPER)
                .list();
    }

    public long compter() {
        return jdbc.sql("SELECT COUNT(*) FROM diagnostic_objet").query(Long.class).single();
    }
}
