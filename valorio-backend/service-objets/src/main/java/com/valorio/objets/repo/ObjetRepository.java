package com.valorio.objets.repo;

import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.StatutObjet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ObjetRepository {

    private static final String SELECT_OBJET = """
            SELECT o.id, o.intitule, o.description, o.categorie_id, o.etat, o.age_annees, o.valeur_estimee,
                   o.ville, o.latitude, o.longitude, o.materiaux, o.proprietaire_id, o.statut,
                   o.date_declaration, o.date_maj, p.url AS photo_url
            FROM objet o
            LEFT JOIN objet_photo p ON p.objet_id = o.id
            """;

    private record LigneObjet(
            Long id,
            String intitule,
            String description,
            Long categorieId,
            String etat,
            Integer ageAnnees,
            Double valeurEstimee,
            String ville,
            Double latitude,
            Double longitude,
            String materiaux,
            Long proprietaireId,
            String statut,
            OffsetDateTime dateDeclaration,
            OffsetDateTime dateMaj,
            String photoUrl
    ) {
    }

    private static final RowMapper<LigneObjet> LIGNE_MAPPER = (rs, i) -> new LigneObjet(
            rs.getLong("id"),
            rs.getString("intitule"),
            rs.getString("description"),
            rs.getLong("categorie_id"),
            rs.getString("etat"),
            rs.getObject("age_annees", Integer.class),
            rs.getObject("valeur_estimee", Double.class),
            rs.getString("ville"),
            rs.getObject("latitude", Double.class),
            rs.getObject("longitude", Double.class),
            rs.getString("materiaux"),
            rs.getLong("proprietaire_id"),
            rs.getString("statut"),
            rs.getObject("date_declaration", OffsetDateTime.class),
            rs.getObject("date_maj", OffsetDateTime.class),
            rs.getString("photo_url"));

    private final JdbcClient jdbc;

    public ObjetRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long creer(String intitule, String description, Long categorieId, EtatObjet etat, Integer ageAnnees,
                      Double valeurEstimee, String ville, Double latitude, Double longitude,
                      List<String> materiaux, Long proprietaireId) {
        return jdbc.sql("""
                        INSERT INTO objet (intitule, description, categorie_id, etat, age_annees, valeur_estimee,
                                           ville, latitude, longitude, materiaux, proprietaire_id, statut)
                        VALUES (:intitule, :description, :categorieId, :etat, :ageAnnees, :valeurEstimee,
                                :ville, :latitude, :longitude, :materiaux, :proprietaireId, 'DECLARE')
                        RETURNING id
                        """)
                .param("intitule", intitule)
                .param("description", description)
                .param("categorieId", categorieId)
                .param("etat", etat.name())
                .param("ageAnnees", ageAnnees)
                .param("valeurEstimee", valeurEstimee)
                .param("ville", ville)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .param("materiaux", versCsv(materiaux))
                .param("proprietaireId", proprietaireId)
                .query(Long.class)
                .single();
    }

    public Optional<Objet> findObjet(long id) {
        return jdbc.sql(SELECT_OBJET + " WHERE o.id = :id ORDER BY p.id")
                .param("id", id)
                .query(LIGNE_MAPPER)
                .list()
                .stream()
                .findFirst()
                .map(lignes -> assembler(List.of(lignes)).get(0));
    }

    public List<Objet> findObjets(Long categorieId, EtatObjet etat, String ville, StatutObjet statut, String q) {
        StringBuilder sql = new StringBuilder(SELECT_OBJET).append(" WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (categorieId != null) {
            sql.append(" AND o.categorie_id = :categorieId");
            params.put("categorieId", categorieId);
        }
        if (etat != null) {
            sql.append(" AND o.etat = :etat");
            params.put("etat", etat.name());
        }
        if (ville != null && !ville.isBlank()) {
            sql.append(" AND o.ville ILIKE :ville");
            params.put("ville", "%" + ville.trim() + "%");
        }
        if (statut != null) {
            sql.append(" AND o.statut = :statut");
            params.put("statut", statut.name());
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (o.intitule ILIKE :q OR o.description ILIKE :q)");
            params.put("q", "%" + q.trim() + "%");
        }
        sql.append(" ORDER BY o.date_declaration DESC, o.id DESC, p.id");

        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return assembler(spec.query(LIGNE_MAPPER).list());
    }

    public void maj(long id, String intitule, String description, Long categorieId, EtatObjet etat,
                    Integer ageAnnees, Double valeurEstimee, String ville, Double latitude, Double longitude,
                    List<String> materiaux, Long proprietaireId) {
        jdbc.sql("""
                        UPDATE objet
                        SET intitule = :intitule, description = :description, categorie_id = :categorieId,
                            etat = :etat, age_annees = :ageAnnees, valeur_estimee = :valeurEstimee,
                            ville = :ville, latitude = :latitude, longitude = :longitude,
                            materiaux = :materiaux, proprietaire_id = :proprietaireId, date_maj = now()
                        WHERE id = :id
                        """)
                .param("intitule", intitule)
                .param("description", description)
                .param("categorieId", categorieId)
                .param("etat", etat.name())
                .param("ageAnnees", ageAnnees)
                .param("valeurEstimee", valeurEstimee)
                .param("ville", ville)
                .param("latitude", latitude)
                .param("longitude", longitude)
                .param("materiaux", versCsv(materiaux))
                .param("proprietaireId", proprietaireId)
                .param("id", id)
                .update();
    }

    public void majStatut(long id, StatutObjet statut) {
        jdbc.sql("UPDATE objet SET statut = :statut, date_maj = now() WHERE id = :id")
                .param("statut", statut.name())
                .param("id", id)
                .update();
    }

    public void supprimer(long id) {
        jdbc.sql("DELETE FROM objet WHERE id = :id").param("id", id).update();
    }

    public long ajouterPhoto(long objetId, String url) {
        return jdbc.sql("""
                        INSERT INTO objet_photo (objet_id, url)
                        VALUES (:objetId, :url)
                        RETURNING id
                        """)
                .param("objetId", objetId)
                .param("url", url)
                .query(Long.class)
                .single();
    }

    public boolean supprimerPhoto(long objetId, long photoId) {
        return jdbc.sql("DELETE FROM objet_photo WHERE id = :id AND objet_id = :objetId")
                .param("id", photoId)
                .param("objetId", objetId)
                .update() > 0;
    }

    public List<String> findPhotos(long objetId) {
        return jdbc.sql("SELECT url FROM objet_photo WHERE objet_id = :objetId ORDER BY id")
                .param("objetId", objetId)
                .query(String.class)
                .list();
    }

    public List<String> listerVilles() {
        return jdbc.sql("SELECT DISTINCT ville FROM objet WHERE ville IS NOT NULL ORDER BY ville")
                .query(String.class)
                .list();
    }

    private static List<Objet> assembler(List<LigneObjet> lignes) {
        Map<Long, Objet> objets = new LinkedHashMap<>();
        Map<Long, List<String>> photos = new LinkedHashMap<>();
        for (LigneObjet ligne : lignes) {
            objets.putIfAbsent(ligne.id(), new Objet(
                    ligne.id(), ligne.intitule(), ligne.description(), ligne.categorieId(),
                    EtatObjet.valueOf(ligne.etat()), ligne.ageAnnees(), ligne.valeurEstimee(),
                    ligne.ville(), ligne.latitude(), ligne.longitude(),
                    versMatieres(ligne.materiaux()), ligne.proprietaireId(),
                    StatutObjet.valueOf(ligne.statut()), List.of(),
                    ligne.dateDeclaration(), ligne.dateMaj()));
            if (ligne.photoUrl() != null) {
                photos.computeIfAbsent(ligne.id(), k -> new ArrayList<>()).add(ligne.photoUrl());
            }
        }
        return objets.values().stream()
                .map(o -> new Objet(o.id(), o.intitule(), o.description(), o.categorieId(), o.etat(), o.ageAnnees(),
                        o.valeurEstimee(), o.ville(), o.latitude(), o.longitude(), o.materiaux(), o.proprietaireId(),
                        o.statut(), photos.getOrDefault(o.id(), List.of()), o.dateDeclaration(), o.dateMaj()))
                .toList();
    }

    public static List<String> versMatieres(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        List<String> matieres = new ArrayList<>();
        for (String valeur : csv.split(",")) {
            if (!valeur.isBlank()) {
                matieres.add(valeur.trim());
            }
        }
        return List.copyOf(matieres);
    }

    public static String versCsv(List<String> matieres) {
        if (matieres == null || matieres.isEmpty()) {
            return null;
        }
        List<String> propres = matieres.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        return propres.isEmpty() ? null : String.join(",", propres);
    }
}
