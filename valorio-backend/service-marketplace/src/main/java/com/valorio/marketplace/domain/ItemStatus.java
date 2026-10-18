package com.valorio.marketplace.domain;

public enum ItemStatus {
    AVAILABLE("Disponible"),
    RESERVED("Réservé"),
    SOLD("Vendu / Attribué"),
    ARCHIVED("Archivé");

    private final String libelle;

    ItemStatus(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
