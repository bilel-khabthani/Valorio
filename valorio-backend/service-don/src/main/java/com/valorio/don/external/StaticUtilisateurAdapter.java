package com.valorio.don.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.don.external.model.UtilisateurRef;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!integ")
@Component
public class StaticUtilisateurAdapter implements UtilisateurPort {

    private final List<UtilisateurRef> utilisateurs;

    public StaticUtilisateurAdapter(ObjectMapper mapper) {
        this.utilisateurs = FixtureLoader.load(mapper, "/fixtures/utilisateur.json", new TypeReference<>() {
        });
    }

    @Override
    public Optional<UtilisateurRef> findById(Long id) {
        return utilisateurs.stream().filter(u -> u.id().equals(id)).findFirst();
    }

    @Override
    public List<UtilisateurRef> findAll() {
        return List.copyOf(utilisateurs);
    }
}
