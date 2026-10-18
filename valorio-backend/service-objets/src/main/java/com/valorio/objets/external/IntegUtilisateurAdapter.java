package com.valorio.objets.external;

import com.valorio.objets.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptateur dynamique : les utilisateurs proviennent du
 * service d'authentification (service-auth, port 8084).
 */
@Component
public class IntegUtilisateurAdapter implements UtilisateurPort {

    private final RestClient restClient;

    public IntegUtilisateurAdapter(
            @Value("${valorio.auth.base-url:http://localhost:8084}") String authBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(authBaseUrl).build();
    }

    @Override
    public Optional<UtilisateurRef> findById(Long id) {
        try {
            UtilisateurRef utilisateur = restClient.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .body(UtilisateurRef.class);
            return Optional.ofNullable(utilisateur);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<UtilisateurRef> findAll() {
        UtilisateurRef[] utilisateurs = restClient.get()
                .uri("/api/users")
                .retrieve()
                .body(UtilisateurRef[].class);
        return utilisateurs == null ? List.of() : List.of(utilisateurs);
    }
}
