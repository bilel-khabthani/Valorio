package com.valorio.reparation.ai.security;

/**
 * Identité de l'utilisateur authentifié déduite du jeton JWT
 * (identifiant interne + acteur métier).
 */
public record UtilisateurConnecte(Long utilisateurId, String acteur) {
}
