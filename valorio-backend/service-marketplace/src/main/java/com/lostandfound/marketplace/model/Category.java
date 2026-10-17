package com.lostandfound.marketplace.model;

public enum Category {
    ELECTRONICS("Électronique & High-Tech"),
    JEWELRY_WATCHES("Bijoux & Montres"),
    BAGS_LUGGAGE("Sacs & Bagages"),
    DOCUMENTS_KEYS("Documents & Clés"),
    CLOTHING_ACCESSORIES("Vêtements & Accessoires"),
    BICYCLES_VEHICLES("Vélos & Mobilité"),
    SPORTS_LEISURE("Sports & Loisirs"),
    OTHER("Autres Objets");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
