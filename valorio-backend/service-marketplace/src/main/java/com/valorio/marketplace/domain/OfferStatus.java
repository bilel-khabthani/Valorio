package com.valorio.marketplace.domain;

public enum OfferStatus {
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    REJECTED("Refusée"),
    COMPLETED("Finalisée");

    private final String libelle;

    OfferStatus(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
