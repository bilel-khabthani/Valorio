package com.valorio.marketplace.dto;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemCondition;
import java.math.BigDecimal;

public record EstimatePriceRequest(
        String title,
        Category category,
        ItemCondition condition,
        BigDecimal originalRetailPrice,
        Integer ageInMonths,
        String brand
) {
}
