package com.valorio.auth.dto;

import com.valorio.auth.domain.Acteur;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'adresse email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
        String password,

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        String confirmPassword,

        Acteur acteur,

        @AssertTrue(message = "Vous devez accepter les conditions générales d'utilisation")
        boolean termsAccepted
) {
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
