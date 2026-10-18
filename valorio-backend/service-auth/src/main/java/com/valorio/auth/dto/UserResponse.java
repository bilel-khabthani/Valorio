package com.valorio.auth.dto;

import com.valorio.auth.domain.Acteur;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;

public record UserResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        String nomComplet,
        Role role,
        Acteur acteur,
        boolean emailVerifie,
        boolean actif,
        String ville,
        Double latitude,
        Double longitude,
        Double scoreReputation
) {

    public UserResponse(Long id, String email, String nom, String prenom, String nomComplet,
                        Role role, boolean emailVerifie, boolean actif) {
        this(id, email, nom, prenom, nomComplet, role,
                role == Role.ADMIN ? Acteur.ADMIN : Acteur.UTILISATEUR, emailVerifie, actif,
                null, null, null, User.SCORE_PAR_DEFAUT);
    }

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.nom(),
                user.prenom(),
                user.nomComplet(),
                user.role(),
                user.acteur() == null
                        ? (user.role() == Role.ADMIN ? Acteur.ADMIN : Acteur.UTILISATEUR)
                        : user.acteur(),
                user.emailVerifie(),
                user.actif(),
                user.ville(),
                user.latitude(),
                user.longitude(),
                user.scoreReputation()
        );
    }
}
