package com.valorio.recommandation.repo;

import com.valorio.recommandation.domain.FeedbackReco;
import com.valorio.recommandation.domain.ModeleMetadataEntity;
import com.valorio.recommandation.domain.RecommandationHistorique;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RecommandationRepository {

    private static final RowMapper<RecommandationHistorique> HISTORIQUE_MAPPER = (rs, i) ->
            new RecommandationHistorique(
                    rs.getLong("id"),
                    rs.getObject("objet_id", Long.class),
                    rs.getString("type_reco"),
                    rs.getString("charge_utilite"),
                    rs.getString("resultat"),
                    rs.getString("source"),
                    rs.getString("modele_version"),
                    rs.getObject("cree_le", OffsetDateTime.class));

    private static final RowMapper<FeedbackReco> FEEDBACK_MAPPER = (rs, i) -> new FeedbackReco(
            rs.getLong("id"),
            rs.getLong("recommandation_id"),
            rs.getBoolean("utile"),
            rs.getString("commentaire"),
            rs.getObject("enregistre_le", OffsetDateTime.class));

    private static final RowMapper<ModeleMetadataEntity> METADATA_MAPPER = (rs, i) -> new ModeleMetadataEntity(
            rs.getLong("id"),
            rs.getString("nom_modele"),
            rs.getString("version"),
            rs.getBoolean("actif"),
            rs.getString("metriques"),
            rs.getString("top_features"),
            rs.getObject("maj_le", OffsetDateTime.class));

    private final JdbcClient jdbc;

    public RecommandationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long insertHistorique(Long objetId, String typeReco, String chargeUtile, String resultat,
                                 String source, String modeleVersion) {
        return jdbc.sql("""
                        INSERT INTO reco_historique (objet_id, type_reco, charge_utilite, resultat, source, modele_version)
                        VALUES (:objetId, :typeReco, :chargeUtile, :resultat, :source, :modeleVersion)
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("typeReco", typeReco)
                .param("chargeUtile", chargeUtile)
                .param("resultat", resultat)
                .param("source", source)
                .param("modeleVersion", modeleVersion)
                .query(Long.class)
                .single();
    }

    public Optional<RecommandationHistorique> findHistorique(long id) {
        return jdbc.sql("SELECT * FROM reco_historique WHERE id = :id")
                .param("id", id)
                .query(HISTORIQUE_MAPPER)
                .optional();
    }

    public List<RecommandationHistorique> listHistorique(Long objetId) {
        if (objetId == null) {
            return jdbc.sql("SELECT * FROM reco_historique ORDER BY cree_le DESC, id DESC")
                    .query(HISTORIQUE_MAPPER)
                    .list();
        }
        return jdbc.sql("SELECT * FROM reco_historique WHERE objet_id = :objetId ORDER BY cree_le DESC, id DESC")
                .param("objetId", objetId)
                .query(HISTORIQUE_MAPPER)
                .list();
    }

    public long insertFeedback(long recommandationId, boolean utile, String commentaire) {
        return jdbc.sql("""
                        INSERT INTO reco_feedback (recommandation_id, utile, commentaire)
                        VALUES (:recommandationId, :utile, :commentaire)
                        RETURNING id
                        """)
                .param("recommandationId", recommandationId)
                .param("utile", utile)
                .param("commentaire", commentaire)
                .query(Long.class)
                .single();
    }

    public Optional<FeedbackReco> findFeedback(long id) {
        return jdbc.sql("SELECT * FROM reco_feedback WHERE id = :id")
                .param("id", id)
                .query(FEEDBACK_MAPPER)
                .optional();
    }

    public void upsertModeleMetadata(String nomModele, String version, boolean actif,
                                     String metriques, String topFeatures) {
        jdbc.sql("""
                        INSERT INTO modele_metadata (nom_modele, version, actif, metriques, top_features)
                        VALUES (:nomModele, :version, :actif, CAST(:metriques AS jsonb), :topFeatures)
                        ON CONFLICT (nom_modele) DO UPDATE
                        SET version = EXCLUDED.version,
                            actif = EXCLUDED.actif,
                            metriques = EXCLUDED.metriques,
                            top_features = EXCLUDED.top_features,
                            maj_le = now()
                        """)
                .param("nomModele", nomModele)
                .param("version", version)
                .param("actif", actif)
                .param("metriques", metriques == null ? "{}" : metriques)
                .param("topFeatures", topFeatures)
                .update();
    }

    public List<ModeleMetadataEntity> findModeleMetadata() {
        return jdbc.sql("SELECT * FROM modele_metadata ORDER BY id")
                .query(METADATA_MAPPER)
                .list();
    }
}
