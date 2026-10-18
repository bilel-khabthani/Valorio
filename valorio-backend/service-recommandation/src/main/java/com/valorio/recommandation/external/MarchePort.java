package com.valorio.recommandation.external;

import com.valorio.recommandation.external.model.ArticleMarcheRef;
import java.util.List;

public interface MarchePort {

    List<ArticleMarcheRef> findAll();
}
