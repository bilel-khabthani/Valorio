package com.valorio.objets.repo;

import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.EvenementHistorique;
import com.valorio.objets.domain.StatutObjet;
import com.valorio.objets.domain.TypeEvenement;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class HistoriqueRepository {

    private static final String SELECT_EVENEMENT =
            "SELECT id, objet_id, type, libelle, acteur_id, statut_cible, date FROM historique_objet";

    private static final RowMapper<EvenementHistorique> EVENEMENT_MAPPER = (rs, i) -> new EvenementHistorique(
            rs.getLong("id"),
            rs.getLong("objet_id"),
            TypeEvenement.valueOf(rs.getString("type")),
            rs.getString("libelle"),
            rs.getObject("acteur_id", Long.class),
            statutOuNull(rs.getString("statut_cible")),
            rs.getObject("date", OffsetDateTime.class));

    private final JdbcClient jdbc;

    public HistoriqueRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long ajouter(long objetId, TypeEvenement type, String libelle, Long acteurId, StatutObjet statutCible) {
        return jdbc.sql("""
                        INSERT INTO historique_objet (objet_id, type, libelle, acteur_id, statut_cible)
                        VALUES (:objetId, :type, :libelle, :acteurId, :statutCible)
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("type", type.name())
                .param("libelle", libelle)
                .param("acteurId", acteurId)
                .param("statutCible", statutCible == null ? null : statutCible.name())
                .query(Long.class)
                .single();
    }

    public List<EvenementHistorique> findEvenements(long objetId) {
        return jdbc.sql(SELECT_EVENEMENT + " WHERE objet_id = :objetId ORDER BY date DESC, id DESC")
                .param("objetId", objetId)
                .query(EVENEMENT_MAPPER)
                .list();
    }

    public long compter() {
        return jdbc.sql("SELECT COUNT(*) FROM historique_objet").query(Long.class).single();
    }

    private static StatutObjet statutOuNull(String valeur) {
        return valeur == null ? null : StatutObjet.valueOf(valeur);
    }
}
