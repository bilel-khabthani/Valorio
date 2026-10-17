package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.DashboardStats;
import com.lostandfound.backend.enums.ClaimStatus;
import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;

    public DashboardStats getStats() {
        return DashboardStats.builder()
                .totalItems(itemRepository.count())
                .totalLost(itemRepository.countByType(ItemType.LOST))
                .totalFound(itemRepository.countByType(ItemType.FOUND))
                .totalActive(itemRepository.countByStatus(ItemStatus.ACTIVE))
                .totalClaimed(itemRepository.countByStatus(ItemStatus.CLAIMED))
                .totalReturned(itemRepository.countByStatus(ItemStatus.RETURNED))
                .totalClaims(claimRepository.count())
                .pendingClaims(claimRepository.countByStatus(ClaimStatus.PENDING))
                .approvedClaims(claimRepository.countByStatus(ClaimStatus.APPROVED))
                .build();
    }
}
