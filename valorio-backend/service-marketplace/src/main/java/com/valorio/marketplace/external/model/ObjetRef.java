package com.valorio.marketplace.external.model;

public record ObjetRef(
        Long id,
        String intitule,
        String statut,
        Long proprietaireId
) {
}
