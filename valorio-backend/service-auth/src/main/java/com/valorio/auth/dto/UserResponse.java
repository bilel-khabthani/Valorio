package com.valorio.auth.dto;

import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;

public record UserResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        String nomComplet,
        Role role,
        boolean emailVerifie,
        boolean actif
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.nom(),
                user.prenom(),
                user.nomComplet(),
                user.role(),
                user.emailVerifie(),
                user.actif()
        );
    }
}
