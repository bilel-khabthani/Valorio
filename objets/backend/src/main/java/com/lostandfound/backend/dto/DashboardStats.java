package com.lostandfound.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {
    private long totalItems;
    private long totalLost;
    private long totalFound;
    private long totalActive;
    private long totalClaimed;
    private long totalReturned;
    private long totalClaims;
    private long pendingClaims;
    private long approvedClaims;
}
