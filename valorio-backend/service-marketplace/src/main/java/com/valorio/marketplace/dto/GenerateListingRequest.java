package com.valorio.marketplace.dto;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import com.valorio.marketplace.domain.ItemType;
import jakarta.validation.constraints.NotBlank;

public record GenerateListingRequest(
        @NotBlank(message = "Les mots-clés ou description brute sont obligatoires") String prompt,
        Category category,
        ItemCondition condition,
        ItemType type,
        String brand,
        String location
) {
}
