package com.valorio.marketplace.repo;

import com.valorio.marketplace.domain.MarketplaceOffer;
import com.valorio.marketplace.domain.OfferStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MarketplaceOfferRepository {

    private static final String SELECT_OFFRE = """
            SELECT o.id AS offer_id, o.buyer_name, o.buyer_email, o.buyer_phone, o.offered_price, o.message,
                   o.status AS offer_status, o.created_at AS offer_created_at,
                   i.id AS it_id, i.title AS it_title, i.description AS it_description, i.category AS it_category,
                   i.item_condition AS it_item_condition, i.item_type AS it_item_type, i.status AS it_status,
                   i.price AS it_price, i.ai_estimated_price AS it_ai_estimated_price,
                   i.ai_price_fairness AS it_ai_price_fairness, i.ai_verification_score AS it_ai_verification_score,
                   i.ai_moderation_notes AS it_ai_moderation_notes, i.original_object_id AS it_original_object_id,
                   i.location AS it_location, i.seller_id AS it_seller_id, i.seller_name AS it_seller_name,
                   i.seller_email AS it_seller_email, i.seller_phone AS it_seller_phone, i.image_url AS it_image_url,
                   i.tags AS it_tags, i.unclaimed_days AS it_unclaimed_days, i.views_count AS it_views_count,
                   i.created_at AS it_created_at, i.updated_at AS it_updated_at
            FROM marketplace_offer o
            JOIN marketplace_item i ON i.id = o.item_id
            """;

    private final JdbcClient jdbc;

    public MarketplaceOfferRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    private static MarketplaceOffer map(ResultSet rs, int i) throws SQLException {
        return new MarketplaceOffer(
                rs.getObject("offer_id", Long.class),
                MarketplaceItemRepository.map(rs, "it_"),
                rs.getString("buyer_name"),
                rs.getString("buyer_email"),
                rs.getString("buyer_phone"),
                rs.getBigDecimal("offered_price"),
                rs.getString("message"),
                OfferStatus.valueOf(rs.getString("offer_status")),
                rs.getObject("offer_created_at", java.time.OffsetDateTime.class));
    }

    public Optional<MarketplaceOffer> findById(Long id) {
        return jdbc.sql(SELECT_OFFRE + " WHERE o.id = :id")
                .param("id", id)
                .query(MarketplaceOfferRepository::map)
                .optional();
    }

    public List<MarketplaceOffer> findByItemId(Long itemId) {
        return jdbc.sql(SELECT_OFFRE + " WHERE o.item_id = :itemId ORDER BY o.created_at DESC, o.id DESC")
                .param("itemId", itemId)
                .query(MarketplaceOfferRepository::map)
                .list();
    }

    public List<MarketplaceOffer> findAll() {
        return jdbc.sql(SELECT_OFFRE + " ORDER BY o.created_at DESC, o.id DESC")
                .query(MarketplaceOfferRepository::map)
                .list();
    }

    public long creer(Long itemId, String buyerName, String buyerEmail, String buyerPhone,
                      java.math.BigDecimal offeredPrice, String message, OfferStatus status) {
        return jdbc.sql("""
                        INSERT INTO marketplace_offer (item_id, buyer_name, buyer_email, buyer_phone, offered_price, message, status)
                        VALUES (:itemId, :buyerName, :buyerEmail, :buyerPhone, :offeredPrice, :message, :status)
                        RETURNING id
                        """)
                .param("itemId", itemId)
                .param("buyerName", buyerName)
                .param("buyerEmail", buyerEmail)
                .param("buyerPhone", buyerPhone)
                .param("offeredPrice", offeredPrice)
                .param("message", message)
                .param("status", status.name())
                .query(Long.class)
                .single();
    }

    public void majStatut(Long id, OfferStatus status) {
        jdbc.sql("UPDATE marketplace_offer SET status = :status WHERE id = :id")
                .param("status", status.name())
                .param("id", id)
                .update();
    }

    public void supprimer(Long id) {
        jdbc.sql("DELETE FROM marketplace_offer WHERE id = :id").param("id", id).update();
    }

    public boolean existe(Long id) {
        return jdbc.sql("SELECT EXISTS(SELECT 1 FROM marketplace_offer WHERE id = :id) AS present")
                .param("id", id)
                .query(Boolean.class)
                .single();
    }

    public long compterParStatut(OfferStatus status) {
        return jdbc.sql("SELECT COUNT(*) FROM marketplace_offer WHERE status = :status")
                .param("status", status.name())
                .query(Long.class)
                .single();
    }
}
