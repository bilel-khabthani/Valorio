package com.valorio.marketplace.repo;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MarketplaceItemRepository {

    static final String COLONNES = "id, title, description, category, item_condition, item_type, status, price, "
            + "ai_estimated_price, ai_price_fairness, ai_verification_score, ai_moderation_notes, original_object_id, "
            + "location, seller_id, seller_name, seller_email, seller_phone, image_url, tags, unclaimed_days, "
            + "views_count, created_at, updated_at";

    private final JdbcClient jdbc;

    public MarketplaceItemRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public static MarketplaceItem map(ResultSet rs, String p) throws SQLException {
        return new MarketplaceItem(
                rs.getObject(p + "id", Long.class),
                rs.getString(p + "title"),
                rs.getString(p + "description"),
                Category.valueOf(rs.getString(p + "category")),
                ItemCondition.valueOf(rs.getString(p + "item_condition")),
                ItemType.valueOf(rs.getString(p + "item_type")),
                ItemStatus.valueOf(rs.getString(p + "status")),
                rs.getBigDecimal(p + "price"),
                rs.getBigDecimal(p + "ai_estimated_price"),
                rs.getString(p + "ai_price_fairness"),
                rs.getObject(p + "ai_verification_score", Double.class),
                rs.getString(p + "ai_moderation_notes"),
                rs.getString(p + "original_object_id"),
                rs.getString(p + "location"),
                rs.getObject(p + "seller_id", Long.class),
                rs.getString(p + "seller_name"),
                rs.getString(p + "seller_email"),
                rs.getString(p + "seller_phone"),
                rs.getString(p + "image_url"),
                rs.getString(p + "tags"),
                rs.getObject(p + "unclaimed_days", Integer.class),
                rs.getObject(p + "views_count", Integer.class),
                rs.getObject(p + "created_at", java.time.OffsetDateTime.class),
                rs.getObject(p + "updated_at", java.time.OffsetDateTime.class));
    }

    public Optional<MarketplaceItem> findById(Long id) {
        return jdbc.sql("SELECT " + COLONNES + " FROM marketplace_item WHERE id = :id")
                .param("id", id)
                .query((rs, i) -> map(rs, ""))
                .optional();
    }

    public List<MarketplaceItem> rechercher(Category category, ItemType type, ItemStatus status,
                                            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String q) {
        StringBuilder sql = new StringBuilder("SELECT " + COLONNES + " FROM marketplace_item WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (category != null) {
            sql.append(" AND category = :category");
            params.put("category", category.name());
        }
        if (type != null) {
            sql.append(" AND item_type = :type");
            params.put("type", type.name());
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status.name());
        }
        if (minPrice != null) {
            sql.append(" AND price >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (title ILIKE :q OR description ILIKE :q OR tags ILIKE :q OR location ILIKE :q OR seller_name ILIKE :q)");
            params.put("q", "%" + q.trim() + "%");
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        JdbcClient.StatementSpec spec = jdbc.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.param(entry.getKey(), entry.getValue());
        }
        return spec.query((rs, i) -> map(rs, "")).list();
    }

    public long creer(MarketplaceItem item) {
        return jdbc.sql("""
                        INSERT INTO marketplace_item (title, description, category, item_condition, item_type, status,
                                                      price, ai_estimated_price, ai_price_fairness, ai_verification_score,
                                                      ai_moderation_notes, original_object_id, location, seller_id,
                                                      seller_name, seller_email, seller_phone, image_url, tags,
                                                      unclaimed_days, views_count)
                        VALUES (:title, :description, :category, :condition, :type, :status, :price, :aiEstimatedPrice,
                                :aiPriceFairness, :aiVerificationScore, :aiModerationNotes, :originalObjectId, :location,
                                :sellerId, :sellerName, :sellerEmail, :sellerPhone, :imageUrl, :tags, :unclaimedDays,
                                :viewsCount)
                        RETURNING id
                        """)
                .param("title", item.title())
                .param("description", item.description())
                .param("category", item.category().name())
                .param("condition", item.condition().name())
                .param("type", item.type().name())
                .param("status", item.status().name())
                .param("price", item.price())
                .param("aiEstimatedPrice", item.aiEstimatedPrice())
                .param("aiPriceFairness", item.aiPriceFairness())
                .param("aiVerificationScore", item.aiVerificationScore())
                .param("aiModerationNotes", item.aiModerationNotes())
                .param("originalObjectId", item.originalObjectId())
                .param("location", item.location())
                .param("sellerId", item.sellerId())
                .param("sellerName", item.sellerName())
                .param("sellerEmail", item.sellerEmail())
                .param("sellerPhone", item.sellerPhone())
                .param("imageUrl", item.imageUrl())
                .param("tags", item.tags())
                .param("unclaimedDays", item.unclaimedDays())
                .param("viewsCount", item.viewsCount())
                .query(Long.class)
                .single();
    }

    public void maj(MarketplaceItem item) {
        jdbc.sql("""
                        UPDATE marketplace_item
                        SET title = :title, description = :description, category = :category, item_condition = :condition,
                            item_type = :type, status = :status, price = :price, location = :location,
                            seller_name = :sellerName, seller_email = :sellerEmail, seller_phone = :sellerPhone,
                            image_url = :imageUrl, tags = :tags, updated_at = now()
                        WHERE id = :id
                        """)
                .param("title", item.title())
                .param("description", item.description())
                .param("category", item.category().name())
                .param("condition", item.condition().name())
                .param("type", item.type().name())
                .param("status", item.status().name())
                .param("price", item.price())
                .param("location", item.location())
                .param("sellerName", item.sellerName())
                .param("sellerEmail", item.sellerEmail())
                .param("sellerPhone", item.sellerPhone())
                .param("imageUrl", item.imageUrl())
                .param("tags", item.tags())
                .param("id", item.id())
                .update();
    }

    public void majStatut(Long id, ItemStatus status) {
        jdbc.sql("UPDATE marketplace_item SET status = :status, updated_at = now() WHERE id = :id")
                .param("status", status.name())
                .param("id", id)
                .update();
    }

    public void incrementerVues(Long id) {
        jdbc.sql("UPDATE marketplace_item SET views_count = COALESCE(views_count, 0) + 1 WHERE id = :id")
                .param("id", id)
                .update();
    }

    public void supprimer(Long id) {
        jdbc.sql("DELETE FROM marketplace_item WHERE id = :id").param("id", id).update();
    }

    public long compter() {
        return jdbc.sql("SELECT COUNT(*) FROM marketplace_item").query(Long.class).single();
    }

    public long compterParStatut(ItemStatus status) {
        return jdbc.sql("SELECT COUNT(*) FROM marketplace_item WHERE status = :status")
                .param("status", status.name())
                .query(Long.class)
                .single();
    }

    public double moyenneScoreIA() {
        Double moyenne = jdbc.sql("SELECT AVG(ai_verification_score) FROM marketplace_item")
                .query(Double.class)
                .optional()
                .orElse(null);
        return moyenne == null ? 95.0 : moyenne;
    }

    public Map<String, Long> compterParCategorie() {
        Map<String, Long> compteurs = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            compteurs.put(category.name(), 0L);
        }
        jdbc.sql("SELECT category, COUNT(*) AS total FROM marketplace_item GROUP BY category")
                .query((rs, i) -> Map.entry(rs.getString("category"), rs.getLong("total")))
                .list()
                .forEach(entree -> compteurs.put(entree.getKey(), entree.getValue()));
        return compteurs;
    }

    public Map<String, Long> compterParType() {
        Map<String, Long> compteurs = new LinkedHashMap<>();
        for (ItemType type : ItemType.values()) {
            compteurs.put(type.name(), 0L);
        }
        jdbc.sql("SELECT item_type, COUNT(*) AS total FROM marketplace_item GROUP BY item_type")
                .query((rs, i) -> Map.entry(rs.getString("item_type"), rs.getLong("total")))
                .list()
                .forEach(entree -> compteurs.put(entree.getKey(), entree.getValue()));
        return compteurs;
    }
}
