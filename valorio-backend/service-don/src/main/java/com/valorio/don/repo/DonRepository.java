package com.valorio.don.repo;

import com.valorio.don.domain.AnnonceDon;
import com.valorio.don.domain.DemandeDon;
import com.valorio.don.domain.HistoriqueStatutDon;
import com.valorio.don.domain.StatutAnnonceDon;
import com.valorio.don.domain.StatutDemandeDon;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DonRepository {

    private static final String SELECT_ANNONCE = """
            SELECT id, objet_id, donneur_id, titre, description, conditions, ville, latitude, longitude,
                   statut, date_publication
            FROM annonce_don
            """;

    private static final String SELECT_DEMANDE = """
            SELECT id, annonce_id, beneficiaire_id, message, statut, date_demande, date_confirmation
            FROM demande_don
            """;

    private static final RowMapper<AnnonceDon> ANNONCE_MAPPER = (rs, i) -> new AnnonceDon(
            rs.getLong("id"),
            rs.getLong("objet_id"),
            rs.getLong("donneur_id"),
            rs.getString("titre"),
            rs.getString("description"),
            rs.getString("conditions"),
            rs.getString("ville"),
            rs.getObject("latitude", Double.class),
            rs.getObject("longitude", Double.class),
            StatutAnnonceDon.valueOf(rs.getString("statut")),
            rs.getObject("date_publication", OffsetDateTime.class));

    private static final RowMapper<DemandeDon> DEMANDE_MAPPER = (rs, i) -> new DemandeDon(
            rs.getLong("id"),
            rs.getLong("annonce_id"),
            rs.getLong("beneficiaire_id"),
            rs.getString("message"),
            StatutDemandeDon.valueOf(rs.getString("statut")),
            rs.getObject("date_demande", OffsetDateTime.class),
            rs.getObject("date_confirmation", OffsetDateTime.class),
            List.of());

    private final JdbcClient jdbc;

    public DonRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creerAnnonce(long objetId, long donneurId, String titre, String description, String conditions,
                             String ville, Double latitude, Double longitude) {
        return jdbc.sql("""
                        INSERT INTO annonce_don (objet_id, donneur_id, titre, description, conditions, ville,
                                                 latitude, longitude, statut)
                        VALUES (:objetId, :donneurId, :titre, :description, :conditions, :ville,
                                :latitude, :longitude, 'PUBLIEE')
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("donneurId", donneurId)
                .param("titre", titre)
                .param("description", description)
                .param("conditions", conditions)
                .param("ville", ville)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .query(Long.class)
                .single();
    }

    public Optional<AnnonceDon> findAnnonce(long id) {
        return jdbc.sql(SELECT_ANNONCE + " WHERE id = :id")
                .param("id", id)
                .query(ANNONCE_MAPPER)
                .optional();
    }

    public List<AnnonceDon> findAnnonces(String ville, StatutAnnonceDon statut) {
        StringBuilder sql = new StringBuilder(SELECT_ANNONCE).append(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (ville != null && !ville.isBlank()) {
            sql.append(" AND ville ILIKE :ville");
            params.put("ville", ville);
        }
        if (statut != null) {
            sql.append(" AND statut = :statut");
            params.put("statut", statut.name());
        }
        sql.append(" ORDER BY date_publication DESC");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query(ANNONCE_MAPPER).list();
    }

    public void majAnnonce(long id, String titre, String description, String conditions, String ville,
                           Double latitude, Double longitude) {
        jdbc.sql("""
                        UPDATE annonce_don
                        SET titre = :titre, description = :description, conditions = :conditions,
                            ville = :ville, latitude = :latitude, longitude = :longitude
                        WHERE id = :id
                        """)
                .param("titre", titre)
                .param("description", description)
                .param("conditions", conditions)
                .param("ville", ville)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .param("id", id)
                .update();
    }

    public void majStatutAnnonce(long id, StatutAnnonceDon statut) {
        jdbc.sql("UPDATE annonce_don SET statut = :statut WHERE id = :id")
                .param("statut", statut.name())
                .param("id", id)
                .update();
    }

    public void supprimerAnnonce(long id) {
        jdbc.sql("DELETE FROM annonce_don WHERE id = :id").param("id", id).update();
    }

    public long creerDemande(long annonceId, long beneficiaireId, String message) {
        return jdbc.sql("""
                        INSERT INTO demande_don (annonce_id, beneficiaire_id, message, statut)
                        VALUES (:annonceId, :beneficiaireId, :message, 'DEMANDE')
                        RETURNING id
                        """)
                .param("annonceId", annonceId)
                .param("beneficiaireId", beneficiaireId)
                .param("message", message)
                .query(Long.class)
                .single();
    }

    public Optional<DemandeDon> findDemande(long id) {
        return jdbc.sql(SELECT_DEMANDE + " WHERE id = :id")
                .param("id", id)
                .query(DEMANDE_MAPPER)
                .optional();
    }

    public List<DemandeDon> findDemandesParAnnonce(long annonceId) {
        return jdbc.sql(SELECT_DEMANDE + " WHERE annonce_id = :annonceId ORDER BY date_demande DESC")
                .param("annonceId", annonceId)
                .query(DEMANDE_MAPPER)
                .list();
    }

    public void majStatutDemande(long id, StatutDemandeDon statut) {
        jdbc.sql("""
                        UPDATE demande_don
                        SET statut = :statut,
                            date_confirmation = CASE WHEN :statut = 'CONFIRMEE' THEN now() ELSE date_confirmation END
                        WHERE id = :id
                        """)
                .param("statut", statut.name())
                .param("id", id)
                .update();
    }

    public void ajouterHistorique(long demandeId, StatutDemandeDon statut, String commentaire) {
        jdbc.sql("""
                        INSERT INTO demande_don_historique (demande_id, statut, commentaire)
                        VALUES (:demandeId, :statut, :commentaire)
                        """)
                .param("demandeId", demandeId)
                .param("statut", statut.name())
                .param("commentaire", commentaire)
                .update();
    }

    public List<HistoriqueStatutDon> findHistorique(long demandeId) {
        return jdbc.sql("""
                        SELECT statut, date, commentaire
                        FROM demande_don_historique
                        WHERE demande_id = :demandeId
                        ORDER BY date
                        """)
                .param("demandeId", demandeId)
                .query((rs, i) -> new HistoriqueStatutDon(
                        StatutDemandeDon.valueOf(rs.getString("statut")),
                        rs.getObject("date", OffsetDateTime.class),
                        rs.getString("commentaire")))
                .list();
    }
}
