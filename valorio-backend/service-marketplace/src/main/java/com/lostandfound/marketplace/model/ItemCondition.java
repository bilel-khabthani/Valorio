package com.lostandfound.marketplace.model;

public enum ItemCondition {
    NEW_UNOPENED("Neuf sous emballage", 1.0),
    EXCELLENT("Comme neuf", 0.85),
    GOOD("Bon état", 0.65),
    FAIR("État moyen / traces d'usure", 0.40),
    FOR_PARTS("Pour pièces / non fonctionnel", 0.15);

    private final String label;
    private final double valueMultiplier;

    ItemCondition(String label, double valueMultiplier) {
        this.label = label;
        this.valueMultiplier = valueMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getValueMultiplier() {
        return valueMultiplier;
    }
}
