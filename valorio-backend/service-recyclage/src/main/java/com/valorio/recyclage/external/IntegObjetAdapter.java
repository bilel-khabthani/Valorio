package com.valorio.recyclage.external;

import com.valorio.recyclage.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adaptateur dynamique : les objets proviennent du
 * service-objets (port 8085).
 */
@Component
public class IntegObjetAdapter implements ObjetPort {

    private final RestClient restClient;

    public IntegObjetAdapter(
            @Value("${valorio.objets.base-url:http://localhost:8085}") String objetsBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(objetsBaseUrl).build();
    }

    @Override
    public Optional<ObjetRef> findById(Long id) {
        try {
            ObjetRef objet = restClient.get()
                    .uri("/api/objets/{id}", id)
                    .retrieve()
                    .body(ObjetRef.class);
            return Optional.ofNullable(objet);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<ObjetRef> findAll(Long categorieId) {
        ObjetRef[] objets = restClient.get()
                .uri("/api/objets")
                .retrieve()
                .body(ObjetRef[].class);
        List<ObjetRef> liste = objets == null ? List.of() : List.of(objets);
        if (categorieId == null) {
            return liste;
        }
        return liste.stream().filter(o -> categorieId.equals(o.categorieId())).toList();
    }
}
