package com.lostandfound.backend.dto;

import com.lostandfound.backend.enums.ClaimStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClaimResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private String claimantName;
    private String claimantContact;
    private String description;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
