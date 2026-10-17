package com.valorio.reparation.external;

import com.valorio.reparation.external.model.CategorieRef;
import java.util.Optional;

public interface CategoriePort {

    Optional<CategorieRef> findById(Long id);
}
