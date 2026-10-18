package com.valorio.recommandation.external;

import com.valorio.recommandation.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);

    List<UtilisateurRef> findAll();
}
