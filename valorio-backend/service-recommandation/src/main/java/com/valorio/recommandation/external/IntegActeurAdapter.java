package com.valorio.recommandation.external;

import com.valorio.recommandation.domain.TypeActeur;
import com.valorio.recommandation.external.model.ActeurRef;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptateur dynamique : les acteurs (réparateurs, bénéficiaires, centres de
 * recyclage) sont dérivés des comptes du microservice d'authentification
 * (service-auth, port 8084) à partir de leur acteur métier.
 */
@Component
public class IntegActeurAdapter implements ActeurPort {

    private final RestClient restClient;

    public IntegActeurAdapter(
            @Value("${valorio.auth.base-url:http://localhost:8084}") String authBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(authBaseUrl).build();
    }

    @Override
    public List<ActeurRef> findAll(TypeActeur typeActeur) {
        if (typeActeur == null) {
            return List.of();
        }
        CompteAuth[] comptes = restClient.get()
                .uri("/api/users")
                .retrieve()
                .body(CompteAuth[].class);
        if (comptes == null) {
            return List.of();
        }
        return java.util.Arrays.stream(comptes)
                .filter(compte -> compte.id() != null)
                .filter(compte -> versType(compte.acteur()) == typeActeur)
                .map(this::versActeur)
                .toList();
    }

    private ActeurRef versActeur(CompteAuth compte) {
        String nomComplet = (valeur(compte.nom()) + " " + valeur(compte.prenom())).trim();
        return new ActeurRef(
                compte.id(),
                compte.id(),
                versType(compte.acteur()),
                nomComplet.isBlank() ? "Compte " + compte.id() : nomComplet,
                compte.ville(),
                compte.latitude() != null ? compte.latitude() : 0.0,
                compte.longitude() != null ? compte.longitude() : 0.0,
                compte.scoreReputation(),
                0,
                compte.actif() == null || compte.actif(),
                null,
                null,
                null);
    }

    private TypeActeur versType(String acteur) {
        if (acteur == null) {
            return TypeActeur.BENEFICIAIRE;
        }
        return switch (acteur.trim().toUpperCase(Locale.ROOT)) {
            case "REPARATEUR" -> TypeActeur.REPARATEUR;
            case "ORGANISME" -> TypeActeur.CENTRE_RECYCLAGE;
            default -> TypeActeur.BENEFICIAIRE;
        };
    }

    private String valeur(String texte) {
        return texte == null ? "" : texte;
    }

    /**
     * Vue partielle de la réponse du service-auth (/api/users) exposant
     * l'acteur métier nécessaire au classement.
     */
    private record CompteAuth(
            Long id,
            String nom,
            String prenom,
            String ville,
            Double latitude,
            Double longitude,
            Double scoreReputation,
            String acteur,
            Boolean actif) {
    }
}
