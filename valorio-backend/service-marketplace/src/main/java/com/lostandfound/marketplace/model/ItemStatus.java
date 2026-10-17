package com.lostandfound.marketplace.model;

public enum ItemStatus {
    AVAILABLE("Disponible"),
    RESERVED("Réservé"),
    SOLD("Vendu / Attribué"),
    ARCHIVED("Archivé");

    private final String label;

    ItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
