package com.valorio.recyclage.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptateur dynamique : journalise l'événement sur le
 * service-objets (POST /api/objets/{id}/historique). Les échecs sont journalisés
 * mais jamais propagés pour ne pas faire échouer la transaction métier locale.
 */
@Component
public class IntegObjetEvenementAdapter implements ObjetEvenementPort {

    private static final Logger log = LoggerFactory.getLogger(IntegObjetEvenementAdapter.class);

    record EvenementPayload(String type, String libelle, Long acteurId, String statutCible) {
    }

    private final RestClient restClient;

    public IntegObjetEvenementAdapter(
            @Value("${valorio.objets.base-url:http://localhost:8085}") String objetsBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(objetsBaseUrl).build();
    }

    @Override
    public void enregistrer(long objetId, String type, String libelle, Long acteurId, String statutCible) {
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
