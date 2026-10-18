package com.valorio.don.external;

/**
 * Remontée des événements métier (don, réparation, recyclage) vers le registre
 * central des objets (service-objets). Les types correspondent à l'énumération
 * {@code TypeEvenement} : DON, REPARATION, RECYCLAGE, TRANSACTION...
 */
public interface ObjetEvenementPort {

    void enregistrer(long objetId, String type, String libelle, Long acteurId, String statutCible);
}
