package com.valorio.don.external;

import com.valorio.don.external.model.CategorieRef;
import java.util.Optional;

public interface CategoriePort {

    Optional<CategorieRef> findById(Long id);
}
