package com.valorio.recyclage.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recyclage.external.model.CategorieRef;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!integ")
@Component
public class StaticCategorieAdapter implements CategoriePort {

    private final List<CategorieRef> categories;

    public StaticCategorieAdapter(ObjectMapper mapper) {
        this.categories = FixtureLoader.load(mapper, "/fixtures/categorie.json", new TypeReference<>() {
        });
    }

    @Override
    public Optional<CategorieRef> findById(Long id) {
        return categories.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}
