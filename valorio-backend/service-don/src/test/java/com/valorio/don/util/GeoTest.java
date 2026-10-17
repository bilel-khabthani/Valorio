package com.valorio.don.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoTest {

    @Test
    void calculeLaDistanceTunisSfax() {
        double distance = Geo.distanceKm(36.8065, 10.1815, 34.7406, 10.7600);
        assertThat(distance).isBetween(225.0, 245.0);
    }

    @Test
    void retourneZeroPourLeMemePoint() {
        assertThat(Geo.distanceKm(36.8065, 10.1815, 36.8065, 10.1815)).isZero();
    }
}
