package com.valorio.reparation.repo;

import com.valorio.reparation.domain.Devis;
import com.valorio.reparation.domain.DemandeReparation;
import com.valorio.reparation.domain.HistoriqueStatut;
import com.valorio.reparation.domain.Reparateur;
import com.valorio.reparation.domain.StatutDemande;
import com.valorio.reparation.domain.StatutDevis;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ReparationRepository {

    private static final String SELECT_DEMANDE = """
            SELECT id, objet_id, utilisateur_id, reparateur_id, description_panne, statut,
                   date_intervention, date_creation, date_maj
            FROM demande_reparation
            """;

    private static final RowMapper<DemandeReparation> DEMANDE_MAPPER = (rs, i) -> new DemandeReparation(
            rs.getLong("id"),
            rs.getLong("objet_id"),
            rs.getLong("utilisateur_id"),
            rs.getObject("reparateur_id", Long.class),
            rs.getString("description_panne"),
            StatutDemande.valueOf(rs.getString("statut")),
            rs.getObject("date_intervention", OffsetDateTime.class),
            rs.getObject("date_creation", OffsetDateTime.class),
            rs.getObject("date_maj", OffsetDateTime.class),
            List.of());

    private static final RowMapper<Devis> DEVIS_MAPPER = (rs, i) -> new Devis(
            rs.getLong("id"),
            rs.getLong("demande_id"),
            rs.getBigDecimal("montant"),
            rs.getObject("delai_jours", Integer.class),
            rs.getString("commentaire"),
            StatutDevis.valueOf(rs.getString("statut")),
            rs.getObject("date_creation", OffsetDateTime.class));

    private static final RowMapper<Reparateur> REPARATEUR_MAPPER = (rs, i) -> new Reparateur(
            rs.getLong("id"),
            rs.getLong("utilisateur_id"),
            rs.getString("ville"),
            rs.getBoolean("disponible"),
            rs.getDouble("note_moyenne"),
            rs.getInt("nb_avis"),
            rs.getString("competences"),
            rs.getString("categories"));

    private final JdbcClient jdbc;

    public ReparationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creerDemande(long objetId, long utilisateurId, String descriptionPanne) {
        return jdbc.sql("""
                        INSERT INTO demande_reparation (objet_id, utilisateur_id, description_panne, statut)
                        VALUES (:objetId, :utilisateurId, :description, 'DEMANDE')
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("utilisateurId", utilisateurId)
                .param("description", descriptionPanne)
                .query(Long.class)
                .single();
    }

    public Optional<DemandeReparation> findDemande(long id) {
        return jdbc.sql(SELECT_DEMANDE + " WHERE id = :id")
                .param("id", id)
                .query(DEMANDE_MAPPER)
                .optional();
    }

    public List<DemandeReparation> findDemandes(StatutDemande statut, Long utilisateurId, Long reparateurId) {
        StringBuilder sql = new StringBuilder(SELECT_DEMANDE).append(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (statut != null) {
            sql.append(" AND statut = :statut");
            params.put("statut", statut.name());
        }
        if (utilisateurId != null) {
            sql.append(" AND utilisateur_id = :utilisateurId");
            params.put("utilisateurId", utilisateurId);
        }
        if (reparateurId != null) {
            sql.append(" AND reparateur_id = :reparateurId");
            params.put("reparateurId", reparateurId);
        }
        sql.append(" ORDER BY date_creation DESC");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query(DEMANDE_MAPPER).list();
    }

    public void majStatut(long id, StatutDemande statut) {
        jdbc.sql("UPDATE demande_reparation SET statut = :statut, date_maj = now() WHERE id = :id")
                .param("statut", statut.name())
                .param("id", id)
                .update();
    }

    public void assignerReparateur(long id, long reparateurId) {
        jdbc.sql("UPDATE demande_reparation SET reparateur_id = :reparateurId, date_maj = now() WHERE id = :id")
                .param("reparateurId", reparateurId)
                .param("id", id)
                .update();
    }

    public void majDateIntervention(long id, OffsetDateTime dateIntervention) {
        jdbc.sql("UPDATE demande_reparation SET date_intervention = :dateIntervention, date_maj = now() WHERE id = :id")
                .param("dateIntervention", dateIntervention)
                .param("id", id)
                .update();
    }

    public void ajouterHistorique(long demandeId, StatutDemande statut, String commentaire) {
        jdbc.sql("""
                        INSERT INTO demande_statut_historique (demande_id, statut, commentaire)
                        VALUES (:demandeId, :statut, :commentaire)
                        """)
                .param("demandeId", demandeId)
                .param("statut", statut.name())
                .param("commentaire", commentaire)
                .update();
    }

    public List<HistoriqueStatut> findHistorique(long demandeId) {
        return jdbc.sql("""
                        SELECT statut, date, commentaire
                        FROM demande_statut_historique
                        WHERE demande_id = :demandeId
                        ORDER BY date
                        """)
                .param("demandeId", demandeId)
                .query((rs, i) -> new HistoriqueStatut(
                        StatutDemande.valueOf(rs.getString("statut")),
                        rs.getObject("date", OffsetDateTime.class),
                        rs.getString("commentaire")))
                .list();
    }

    public long creerDevis(long demandeId, BigDecimal montant, Integer delaiJours, String commentaire) {
        return jdbc.sql("""
                        INSERT INTO devis (demande_id, montant, delai_jours, commentaire, statut)
                        VALUES (:demandeId, :montant, :delai, :commentaire, 'EN_ATTENTE')
                        RETURNING id
                        """)
                .param("demandeId", demandeId)
                .param("montant", montant)
                .param("delai", delaiJours)
                .param("commentaire", commentaire)
                .query(Long.class)
                .single();
    }

    public Optional<Devis> findDevis(long devisId) {
        return jdbc.sql("SELECT * FROM devis WHERE id = :id")
                .param("id", devisId)
                .query(DEVIS_MAPPER)
                .optional();
    }

    public Optional<Devis> findDevisParDemande(long demandeId) {
        return jdbc.sql("SELECT * FROM devis WHERE demande_id = :demandeId ORDER BY date_creation DESC LIMIT 1")
                .param("demandeId", demandeId)
                .query(DEVIS_MAPPER)
                .optional();
    }

    public void majStatutDevis(long devisId, StatutDevis statut) {
        jdbc.sql("UPDATE devis SET statut = :statut WHERE id = :id")
                .param("statut", statut.name())
                .param("id", devisId)
                .update();
    }

    public List<Reparateur> rechercherReparateurs(String categorie, String competence, String ville) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reparateur WHERE disponible = TRUE");
        Map<String, Object> params = new HashMap<>();
        if (categorie != null && !categorie.isBlank()) {
            sql.append(" AND categories ILIKE :categorie");
            params.put("categorie", "%" + categorie + "%");
        }
        if (competence != null && !competence.isBlank()) {
            sql.append(" AND competences ILIKE :competence");
            params.put("competence", "%" + competence + "%");
        }
        if (ville != null && !ville.isBlank()) {
            sql.append(" AND ville ILIKE :ville");
            params.put("ville", ville);
        }
        sql.append(" ORDER BY note_moyenne DESC");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query(REPARATEUR_MAPPER).list();
    }

    public Optional<Reparateur> findReparateur(long id) {
        return jdbc.sql("SELECT * FROM reparateur WHERE id = :id")
                .param("id", id)
                .query(REPARATEUR_MAPPER)
                .optional();
    }
}
