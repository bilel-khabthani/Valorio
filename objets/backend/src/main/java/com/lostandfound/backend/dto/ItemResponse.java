package com.lostandfound.backend.dto;

import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private LocalDate date;
    private String photoUrl;
    private ItemType type;
    private ItemStatus status;
    private String reporterName;
    private String reporterContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int claimsCount;
}
