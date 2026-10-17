package com.lostandfound.backend.repository;

import com.lostandfound.backend.entity.Item;
import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByType(ItemType type);

    List<Item> findByStatus(ItemStatus status);

    // Recherche par keyword seul (title/description)
    @Query("SELECT i FROM Item i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.createdAt DESC")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    // Recherche par type
    @Query("SELECT i FROM Item i WHERE i.type = :type ORDER BY i.createdAt DESC")
    List<Item> findByTypeSorted(@Param("type") ItemType type);

    // Recherche par statut
    @Query("SELECT i FROM Item i WHERE i.status = :status ORDER BY i.createdAt DESC")
    List<Item> findByStatusSorted(@Param("status") ItemStatus status);

    // Recherche par catégorie
    @Query("SELECT i FROM Item i WHERE LOWER(i.category) = LOWER(:category) ORDER BY i.createdAt DESC")
    List<Item> findByCategory(@Param("category") String category);

    // Tous triés par date
    @Query("SELECT i FROM Item i ORDER BY i.createdAt DESC")
    List<Item> findAllSorted();

    // Statistiques
    long countByType(ItemType type);
    long countByStatus(ItemStatus status);
    long countByTypeAndStatus(ItemType type, ItemStatus status);

    @Query("SELECT DISTINCT i.category FROM Item i WHERE i.category IS NOT NULL ORDER BY i.category")
    List<String> findAllCategories();

    @Query("SELECT DISTINCT i.location FROM Item i WHERE i.location IS NOT NULL ORDER BY i.location")
    List<String> findAllLocations();
}
