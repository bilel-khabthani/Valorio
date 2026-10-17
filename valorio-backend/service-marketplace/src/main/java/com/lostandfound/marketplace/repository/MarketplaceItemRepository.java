package com.lostandfound.marketplace.repository;

import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemStatus;
import com.lostandfound.marketplace.model.ItemType;
import com.lostandfound.marketplace.model.MarketplaceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, Long> {

    List<MarketplaceItem> findByStatus(ItemStatus status);

    List<MarketplaceItem> findByCategory(Category category);

    List<MarketplaceItem> findByType(ItemType type);

    @Query("SELECT m FROM MarketplaceItem m WHERE " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:type IS NULL OR m.type = :type) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR m.price <= :maxPrice) AND " +
           "(:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.tags) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY m.createdAt DESC")
    List<MarketplaceItem> searchItems(
            @Param("category") Category category,
            @Param("type") ItemType type,
            @Param("status") ItemStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search);

    @Query("SELECT AVG(m.price) FROM MarketplaceItem m WHERE m.category = :category AND m.status <> 'ARCHIVED'")
    Double getAveragePriceByCategory(@Param("category") Category category);

    @Query("SELECT COUNT(m) FROM MarketplaceItem m WHERE m.status = :status")
    Long countByStatus(@Param("status") ItemStatus status);

    @Query("SELECT COUNT(m) FROM MarketplaceItem m WHERE m.type = :type")
    Long countByType(@Param("type") ItemType type);
}
