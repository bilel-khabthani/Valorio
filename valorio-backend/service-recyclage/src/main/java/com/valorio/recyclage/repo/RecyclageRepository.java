package com.valorio.recyclage.repo;

import com.valorio.recyclage.domain.CentreRecyclage;
import com.valorio.recyclage.domain.DemandeCollecte;
import com.valorio.recyclage.domain.HistoriqueCollecte;
import com.valorio.recyclage.domain.StatutCollecte;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RecyclageRepository {

    private static final String SELECT_CENTRE = """
            SELECT id, organisme_id, nom, ville, adresse, latitude, longitude, materiaux_acceptes, horaires
            FROM centre_recyclage
            """;

    private static final String SELECT_COLLECTE = """
            SELECT id, objet_id, utilisateur_id, centre_id, statut, date_demande, date_prevue, date_reception
            FROM demande_collecte
            """;

    private static final RowMapper<CentreRecyclage> CENTRE_MAPPER = (rs, i) -> new CentreRecyclage(
            rs.getLong("id"),
            rs.getObject("organisme_id", Long.class),
            rs.getString("nom"),
            rs.getString("ville"),
            rs.getString("adresse"),
            rs.getObject("latitude", Double.class),
            rs.getObject("longitude", Double.class),
            split(rs.getString("materiaux_acceptes")),
            rs.getString("horaires"),
            null);

    private static final RowMapper<DemandeCollecte> COLLECTE_MAPPER = (rs, i) -> new DemandeCollecte(
            rs.getLong("id"),
            rs.getLong("objet_id"),
            rs.getLong("utilisateur_id"),
            rs.getObject("centre_id", Long.class),
            StatutCollecte.valueOf(rs.getString("statut")),
            rs.getObject("date_demande", OffsetDateTime.class),
            rs.getObject("date_prevue", OffsetDateTime.class),
            rs.getObject("date_reception", OffsetDateTime.class),
            List.of());

    private final JdbcClient jdbc;

    public RecyclageRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creerCentre(Long organismeId, String nom, String ville, String adresse, Double latitude,
                            Double longitude, List<String> materiauxAcceptes, String horaires) {
        return jdbc.sql("""
                        INSERT INTO centre_recyclage (organisme_id, nom, ville, adresse, latitude, longitude,
                                                      materiaux_acceptes, horaires)
                        VALUES (:organismeId, :nom, :ville, :adresse, :latitude, :longitude,
                                :materiaux, :horaires)
                        RETURNING id
                        """)
                .param("organismeId", organismeId)
                .param("nom", nom)
                .param("ville", ville)
                .param("adresse", adresse)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .param("materiaux", String.join(",", materiauxAcceptes))
                .param("horaires", horaires)
                .query(Long.class)
                .single();
    }

    public Optional<CentreRecyclage> findCentre(long id) {
        return jdbc.sql(SELECT_CENTRE + " WHERE id = :id")
                .param("id", id)
                .query(CENTRE_MAPPER)
                .optional();
    }

    public List<CentreRecyclage> findCentres(String materiau) {
        if (materiau == null || materiau.isBlank()) {
            return jdbc.sql(SELECT_CENTRE + " ORDER BY nom")
                    .query(CENTRE_MAPPER)
                    .list();
        }
        return jdbc.sql(SELECT_CENTRE + " WHERE materiaux_acceptes ILIKE :materiau ORDER BY nom")
                .param("materiau", "%" + materiau + "%")
                .query(CENTRE_MAPPER)
                .list();
    }

    public void majCentre(long id, Long organismeId, String nom, String ville, String adresse, Double latitude,
                          Double longitude, List<String> materiauxAcceptes, String horaires) {
        jdbc.sql("""
                        UPDATE centre_recyclage
                        SET organisme_id = :organismeId, nom = :nom, ville = :ville, adresse = :adresse,
                            latitude = :latitude, longitude = :longitude, materiaux_acceptes = :materiaux,
                            horaires = :horaires
                        WHERE id = :id
                        """)
                .param("organismeId", organismeId)
                .param("nom", nom)
                .param("ville", ville)
                .param("adresse", adresse)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .param("materiaux", String.join(",", materiauxAcceptes))
                .param("horaires", horaires)
                .param("id", id)
                .update();
    }

    public void supprimerCentre(long id) {
        jdbc.sql("DELETE FROM centre_recyclage WHERE id = :id")
                .param("id", id)
                .update();
    }

    public int compteCollectes(long centreId) {
        return jdbc.sql("SELECT COUNT(*) FROM demande_collecte WHERE centre_id = :id")
                .param("id", centreId)
                .query(Integer.class)
                .single();
    }

    public long creerCollecte(long objetId, long utilisateurId, Long centreId, OffsetDateTime datePrevue) {
        return jdbc.sql("""
                        INSERT INTO demande_collecte (objet_id, utilisateur_id, centre_id, statut, date_prevue)
                        VALUES (:objetId, :utilisateurId, :centreId, 'DEMANDE', :datePrevue)
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("utilisateurId", utilisateurId)
                .param("centreId", centreId)
                .param("datePrevue", datePrevue)
                .query(Long.class)
                .single();
    }

    public Optional<DemandeCollecte> findCollecte(long id) {
        return jdbc.sql(SELECT_COLLECTE + " WHERE id = :id")
                .param("id", id)
                .query(COLLECTE_MAPPER)
                .optional();
    }

    public List<DemandeCollecte> findCollectes(StatutCollecte statut, Long utilisateurId, Long centreId) {
        StringBuilder sql = new StringBuilder(SELECT_COLLECTE).append(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (statut != null) {
            sql.append(" AND statut = :statut");
            params.put("statut", statut.name());
        }
        if (utilisateurId != null) {
            sql.append(" AND utilisateur_id = :utilisateurId");
            params.put("utilisateurId", utilisateurId);
        }
        if (centreId != null) {
            sql.append(" AND centre_id = :centreId");
            params.put("centreId", centreId);
        }
        sql.append(" ORDER BY date_demande DESC");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query(COLLECTE_MAPPER).list();
    }

    public void majStatutCollecte(long id, StatutCollecte statut, OffsetDateTime datePrevue) {
        jdbc.sql("""
                        UPDATE demande_collecte
                        SET statut = :statut,
                            date_prevue = COALESCE(:datePrevue, date_prevue),
                            date_reception = CASE WHEN :statut = 'RECUE' THEN now() ELSE date_reception END
                        WHERE id = :id
                        """)
                .param("statut", statut.name())
                .param("datePrevue", datePrevue)
                .param("id", id)
                .update();
    }

    public void ajouterHistorique(long collecteId, StatutCollecte statut, String commentaire) {
        jdbc.sql("""
                        INSERT INTO collecte_historique (collecte_id, statut, commentaire)
                        VALUES (:collecteId, :statut, :commentaire)
                        """)
                .param("collecteId", collecteId)
                .param("statut", statut.name())
                .param("commentaire", commentaire)
                .update();
    }

    public List<HistoriqueCollecte> findHistorique(long collecteId) {
        return jdbc.sql("""
                        SELECT statut, date, commentaire
                        FROM collecte_historique
                        WHERE collecte_id = :collecteId
                        ORDER BY date
                        """)
                .param("collecteId", collecteId)
                .query((rs, i) -> new HistoriqueCollecte(
                        StatutCollecte.valueOf(rs.getString("statut")),
                        rs.getObject("date", OffsetDateTime.class),
                        rs.getString("commentaire")))
                .list();
    }

    private static List<String> split(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return List.of();
        }
        return List.of(valeur.split(","));
    }
}
