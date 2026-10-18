package com.valorio.marketplace.external;

import com.valorio.marketplace.external.model.UtilisateurRef;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);
}
