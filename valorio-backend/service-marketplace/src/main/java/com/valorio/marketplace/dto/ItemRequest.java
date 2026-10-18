package com.valorio.marketplace.dto;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank(message = "Le titre est obligatoire") String title,
        @NotBlank(message = "La description est obligatoire") String description,
        @NotNull(message = "La catégorie est obligatoire") Category category,
        @NotNull(message = "L'état est obligatoire") ItemCondition condition,
        @NotNull(message = "Le type d'annonce est obligatoire") ItemType type,
        ItemStatus status,
        BigDecimal price,
        String originalObjectId,
        String location,
        Long sellerId,
        String sellerName,
        String sellerEmail,
        String sellerPhone,
        String imageUrl,
        String tags,
        Integer unclaimedDays
) {
}
