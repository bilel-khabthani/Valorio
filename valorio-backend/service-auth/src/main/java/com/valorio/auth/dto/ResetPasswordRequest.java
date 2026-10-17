package com.valorio.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Le jeton de réinitialisation est obligatoire")
        String token,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
        String newPassword,

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        String confirmPassword
) {
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
