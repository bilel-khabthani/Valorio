package com.lostandfound.marketplace.repository;

import com.lostandfound.marketplace.model.MarketplaceOffer;
import com.lostandfound.marketplace.model.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceOfferRepository extends JpaRepository<MarketplaceOffer, Long> {

    List<MarketplaceOffer> findByItemIdOrderByCreatedAtDesc(Long itemId);

    List<MarketplaceOffer> findByStatusOrderByCreatedAtDesc(OfferStatus status);

    List<MarketplaceOffer> findAllByOrderByCreatedAtDesc();

    Long countByStatus(OfferStatus status);
}
