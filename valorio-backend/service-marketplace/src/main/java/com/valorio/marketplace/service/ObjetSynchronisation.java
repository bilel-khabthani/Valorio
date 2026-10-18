package com.valorio.marketplace.service;

import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.external.ObjetPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Remontée best-effort vers le service-objets lorsqu'un article de la
 * marketplace est vendu. L'identifiant d'objet d'origine est facultatif :
 * si {@code originalObjectId} n'est pas un identifiant numérique d'objet,
 * la synchronisation est simplement ignorée.
 */
@Component
public class ObjetSynchronisation {

    private static final Logger log = LoggerFactory.getLogger(ObjetSynchronisation.class);

    private final ObjetPort objets;

    public ObjetSynchronisation(ObjetPort objets) {
        this.objets = objets;
    }

    public void signalerVente(MarketplaceItem item) {
        Long objetId = identifiantObjet(item.originalObjectId());
        if (objetId == null) {
            return;
        }
        try {
            objets.enregistrerEvenement(objetId, "TRANSACTION",
                    "Objet vendu via la marketplace : " + item.title(), item.sellerId(), "VENDU");
        } catch (Exception e) {
            log.warn("Synchronisation objet impossible (item={}, objet={}) : {}",
                    item.id(), objetId, e.getMessage());
        }
    }

    private Long identifiantObjet(String originalObjectId) {
        if (originalObjectId == null || originalObjectId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(originalObjectId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
