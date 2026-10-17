package com.valorio.reparation.external;

import com.valorio.reparation.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;

public interface ObjetPort {

    Optional<ObjetRef> findById(Long id);

    List<ObjetRef> findAll(Long categorieId);
}
