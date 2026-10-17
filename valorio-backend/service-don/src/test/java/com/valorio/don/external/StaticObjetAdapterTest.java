package com.valorio.don.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.don.external.model.ObjetRef;
import org.junit.jupiter.api.Test;

class StaticObjetAdapterTest {

    private final StaticObjetAdapter adapter = new StaticObjetAdapter(new ObjectMapper());

    @Test
    void trouveUnObjetParId() {
        assertThat(adapter.findById(1L)).isPresent()
                .get()
                .extracting(ObjetRef::intitule)
                .asString()
                .contains("Ordinateur portable");
    }

    @Test
    void retourneNullQuandObjetInconnu() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void filtreParCategorie() {
        assertThat(adapter.findAll(1L)).hasSize(2);
        assertThat(adapter.findAll(null)).hasSize(5);
    }
}
