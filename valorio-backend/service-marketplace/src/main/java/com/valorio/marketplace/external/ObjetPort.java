package com.valorio.marketplace.external;

import com.valorio.marketplace.external.model.ObjetRef;
import java.util.Optional;

public interface ObjetPort {

    Optional<ObjetRef> findById(Long id);

    void enregistrerEvenement(Long objetId, String type, String libelle, Long acteurId, String statutCible);
}
