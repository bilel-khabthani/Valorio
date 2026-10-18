package com.valorio.recommandation.external;

import com.valorio.recommandation.dto.ArticleMarche;
import com.valorio.recommandation.external.model.ArticleMarcheRef;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Adaptateur dynamique : le marché provient du microservice Marketplace
 * (service-marketplace, port 8086) via {@link MarketplaceMarcheClient}.
 * Aucune donnée statique n'est utilisée en repli : si le service est
 * indisponible, la liste retournée est vide.
 */
@Component
public class IntegMarcheAdapter implements MarchePort {

    private final MarketplaceMarcheClient marketplace;

    public IntegMarcheAdapter(MarketplaceMarcheClient marketplace) {
        this.marketplace = marketplace;
    }

    @Override
    public List<ArticleMarcheRef> findAll() {
        return marketplace.findAllDisponibles().stream()
                .map(this::versRef)
                .toList();
    }

    private ArticleMarcheRef versRef(ArticleMarche article) {
        return new ArticleMarcheRef(article.id(), article.titre(), article.categorie(),
                article.etat(), article.prix(), article.description());
    }
}
