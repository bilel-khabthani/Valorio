package com.valorio.recommandation.external;

import com.valorio.recommandation.domain.TypeActeur;
import com.valorio.recommandation.external.model.ActeurRef;
import java.util.List;

public interface ActeurPort {

    List<ActeurRef> findAll(TypeActeur typeActeur);
}
