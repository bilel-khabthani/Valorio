package com.valorio.don.external;

import com.valorio.don.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;

public interface UtilisateurPort {

    Optional<UtilisateurRef> findById(Long id);

    List<UtilisateurRef> findAll();
}
