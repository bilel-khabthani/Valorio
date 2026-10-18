package com.valorio.marketplace.domain;

public enum ItemCondition {
    NEW_UNOPENED("Neuf sous emballage", 1.0),
    EXCELLENT("Comme neuf", 0.85),
    GOOD("Bon état", 0.65),
    FAIR("État moyen / traces d'usure", 0.40),
    FOR_PARTS("Pour pièces / non fonctionnel", 0.15);

    private final String libelle;
    private final double coefficientValeur;

    ItemCondition(String libelle, double coefficientValeur) {
        this.libelle = libelle;
        this.coefficientValeur = coefficientValeur;
    }

    public String libelle() {
        return libelle;
    }

    public double coefficientValeur() {
        return coefficientValeur;
    }
}
