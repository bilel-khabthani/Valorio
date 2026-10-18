package com.valorio.objets.repo;

import com.valorio.objets.domain.Categorie;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CategorieRepository {

    private static final String SELECT_CATEGORIE = "SELECT id, nom, description FROM categorie";

    private static final RowMapper<Categorie> CATEGORIE_MAPPER = (rs, i) -> new Categorie(
            rs.getLong("id"),
            rs.getString("nom"),
            rs.getString("description"));

    private final JdbcClient jdbc;

    public CategorieRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creer(String nom, String description) {
        return jdbc.sql("""
                        INSERT INTO categorie (nom, description)
                        VALUES (:nom, :description)
                        RETURNING id
                        """)
                .param("nom", nom)
                .param("description", description)
                .query(Long.class)
                .single();
    }

    public Optional<Categorie> findCategorie(long id) {
        return jdbc.sql(SELECT_CATEGORIE + " WHERE id = :id")
                .param("id", id)
                .query(CATEGORIE_MAPPER)
                .optional();
    }

    public List<Categorie> findCategories() {
        return jdbc.sql(SELECT_CATEGORIE + " ORDER BY nom")
                .query(CATEGORIE_MAPPER)
                .list();
    }

    public boolean existeNom(String nom) {
        return jdbc.sql("SELECT COUNT(*) FROM categorie WHERE lower(nom) = lower(:nom)")
                .param("nom", nom)
                .query(Integer.class)
                .single() > 0;
    }

    public void maj(long id, String nom, String description) {
        jdbc.sql("UPDATE categorie SET nom = :nom, description = :description WHERE id = :id")
                .param("nom", nom)
                .param("description", description)
                .param("id", id)
                .update();
    }

    public void supprimer(long id) {
        jdbc.sql("DELETE FROM categorie WHERE id = :id")
                .param("id", id)
                .update();
    }

    public boolean existeNomSauf(String nom, long id) {
        return jdbc.sql("SELECT COUNT(*) FROM categorie WHERE lower(nom) = lower(:nom) AND id <> :id")
                .param("nom", nom)
                .param("id", id)
                .query(Integer.class)
                .single() > 0;
    }

    public int compteObjets(long categorieId) {
        return jdbc.sql("SELECT COUNT(*) FROM objet WHERE categorie_id = :id")
                .param("id", categorieId)
                .query(Integer.class)
                .single();
    }
}
