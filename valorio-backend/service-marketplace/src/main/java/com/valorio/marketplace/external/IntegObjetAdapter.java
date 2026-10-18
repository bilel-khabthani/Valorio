package com.valorio.marketplace.external;

import com.valorio.marketplace.external.model.ObjetRef;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adaptateur dynamique vers le service-objets (port 8085) : lecture d'un objet
 * et remontée d'événements (ex. article vendu sur la marketplace).
 */
@Component
public class IntegObjetAdapter implements ObjetPort {

    private static final Logger log = LoggerFactory.getLogger(IntegObjetAdapter.class);

    record EvenementPayload(String type, String libelle, Long acteurId, String statutCible) {
    }

    private final RestClient restClient;

    public IntegObjetAdapter(
            @Value("${valorio.objets.base-url:http://localhost:8085}") String objetsBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(objetsBaseUrl).build();
    }

    @Override
    public Optional<ObjetRef> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
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
    public void enregistrerEvenement(Long objetId, String type, String libelle, Long acteurId, String statutCible) {
        try {
            restClient.post()
                    .uri("/api/objets/{id}/historique", objetId)
                    .body(new EvenementPayload(type, libelle, acteurId, statutCible))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Remontée événement objet impossible (objet={}, type={}) : {}",
                    objetId, type, e.getMessage());
        }
    }
}
