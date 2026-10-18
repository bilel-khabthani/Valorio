package com.valorio.marketplace.domain;

public enum ItemType {
    SALE("Vente d'objet non réclamé"),
    DONATION("Don solidaire / Gratuit"),
    REWARD_BOUNTY("Récompense / Avis de recherche");

    private final String libelle;

    ItemType(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
