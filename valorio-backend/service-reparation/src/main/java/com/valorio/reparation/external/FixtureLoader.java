package com.valorio.reparation.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class FixtureLoader {

    private FixtureLoader() {
    }

    public static <T> List<T> load(ObjectMapper mapper, String classpathLocation, TypeReference<List<T>> type) {
        try (InputStream in = FixtureLoader.class.getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Fixture introuvable : " + classpathLocation);
            }
            return mapper.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Fixture illisible : " + classpathLocation, e);
        }
    }
}
