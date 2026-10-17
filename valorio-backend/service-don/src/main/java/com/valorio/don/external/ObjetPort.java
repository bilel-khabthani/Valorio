package com.valorio.don.external;

import com.valorio.don.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;

public interface ObjetPort {

    Optional<ObjetRef> findById(Long id);

    List<ObjetRef> findAll(Long categorieId);
}
