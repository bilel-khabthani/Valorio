package com.valorio.reparation.external;

import com.valorio.reparation.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);

    List<UtilisateurRef> findAll();
}
