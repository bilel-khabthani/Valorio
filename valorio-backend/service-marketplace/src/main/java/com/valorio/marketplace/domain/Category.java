package com.valorio.marketplace.domain;

public enum Category {
    ELECTRONICS("Électronique & High-Tech"),
    JEWELRY_WATCHES("Bijoux & Montres"),
    BAGS_LUGGAGE("Sacs & Bagages"),
    DOCUMENTS_KEYS("Documents & Clés"),
    CLOTHING_ACCESSORIES("Vêtements & Accessoires"),
    BICYCLES_VEHICLES("Vélos & Mobilité"),
    SPORTS_LEISURE("Sports & Loisirs"),
    OTHER("Autres Objets");

    private final String libelle;

    Category(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
