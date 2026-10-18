package com.valorio.recommandation.external;

import com.valorio.recommandation.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;

public interface ObjetPort {

    Optional<ObjetRef> findById(Long id);

    List<ObjetRef> findAll(Long categorieId);
}
