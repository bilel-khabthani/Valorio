package com.valorio.recyclage.external;

import com.valorio.recyclage.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;

public interface ObjetPort {

    Optional<ObjetRef> findById(Long id);

    List<ObjetRef> findAll(Long categorieId);
}
