package com.lostandfound.marketplace.model;

public enum ItemType {
    SALE("Vente d'objet non réclamé"),
    DONATION("Don solidaire / Gratuit"),
    REWARD_BOUNTY("Récompense / Avis de recherche");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
