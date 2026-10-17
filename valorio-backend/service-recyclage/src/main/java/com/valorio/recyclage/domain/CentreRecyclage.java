package com.valorio.recyclage.domain;

import java.util.List;

public record CentreRecyclage(
        Long id,
        Long organismeId,
        String nom,
        String ville,
        String adresse,
        Double latitude,
        Double longitude,
        List<String> materiauxAcceptes,
        String horaires,
        Double distanceKm
) {
    public CentreRecyclage avecDistance(Double distance) {
        return new CentreRecyclage(id, organismeId, nom, ville, adresse, latitude, longitude,
                materiauxAcceptes, horaires, distance);
    }
}
