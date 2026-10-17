package com.valorio.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Le jeton de vérification est obligatoire")
        String token
) {}
