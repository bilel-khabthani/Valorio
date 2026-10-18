package com.valorio.objets.external;

import com.valorio.objets.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);

    List<UtilisateurRef> findAll();
}
