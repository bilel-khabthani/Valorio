package com.valorio.recyclage.external;

import com.valorio.recyclage.external.model.CategorieRef;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adaptateur dynamique : les catégories proviennent du
 * service-objets (port 8085).
 */
@Component
public class IntegCategorieAdapter implements CategoriePort {

    private final RestClient restClient;

    public IntegCategorieAdapter(
            @Value("${valorio.objets.base-url:http://localhost:8085}") String objetsBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(objetsBaseUrl).build();
    }

    @Override
    public Optional<CategorieRef> findById(Long id) {
        try {
            CategorieRef categorie = restClient.get()
                    .uri("/api/categories/{id}", id)
                    .retrieve()
                    .body(CategorieRef.class);
            return Optional.ofNullable(categorie);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
