package com.valorio.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'adresse email invalide")
        String email
) {}
