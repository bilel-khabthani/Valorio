package com.valorio.don.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.don.external.model.ObjetRef;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!integ")
@Component
public class StaticObjetAdapter implements ObjetPort {

    private final List<ObjetRef> objets;

    public StaticObjetAdapter(ObjectMapper mapper) {
        this.objets = FixtureLoader.load(mapper, "/fixtures/objet.json", new TypeReference<>() {
        });
    }

    @Override
    public Optional<ObjetRef> findById(Long id) {
        return objets.stream().filter(o -> o.id().equals(id)).findFirst();
    }

    @Override
    public List<ObjetRef> findAll(Long categorieId) {
        if (categorieId == null) {
            return objets;
        }
        return objets.stream().filter(o -> categorieId.equals(o.categorieId())).toList();
    }
}
