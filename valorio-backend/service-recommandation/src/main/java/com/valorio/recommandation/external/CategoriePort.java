package com.valorio.recommandation.external;

import com.valorio.recommandation.external.model.CategorieRef;
import java.util.Optional;

public interface CategoriePort {

    Optional<CategorieRef> findById(Long id);
}
