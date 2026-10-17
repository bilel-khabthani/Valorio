package com.valorio.recyclage.external;

import com.valorio.recyclage.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);

    List<UtilisateurRef> findAll();
}
