package com.valorio.recyclage.external;

import com.valorio.recyclage.external.model.CategorieRef;
import java.util.Optional;

public interface CategoriePort {

    Optional<CategorieRef> findById(Long id);
}
