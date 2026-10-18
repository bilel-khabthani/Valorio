package com.valorio.marketplace.dto;

import com.valorio.marketplace.domain.Category;

public record ModerationRequest(
        String title,
        String description,
        Category category,
        String price
) {
}
