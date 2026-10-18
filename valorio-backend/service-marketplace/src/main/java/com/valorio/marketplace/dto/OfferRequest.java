package com.valorio.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record OfferRequest(
        @NotBlank(message = "Le nom de l'acheteur est obligatoire") String buyerName,
        @NotBlank(message = "L'email de l'acheteur est obligatoire") String buyerEmail,
        String buyerPhone,
        BigDecimal offeredPrice,
        String message
) {
}
