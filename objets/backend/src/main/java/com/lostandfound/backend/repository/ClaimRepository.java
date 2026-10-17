package com.lostandfound.backend.repository;

import com.lostandfound.backend.entity.Claim;
import com.lostandfound.backend.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByItemId(Long itemId);

    List<Claim> findByStatus(ClaimStatus status);

    long countByStatus(ClaimStatus status);

    boolean existsByItemIdAndClaimantContact(Long itemId, String claimantContact);
}
