package com.valorio.reparation.domain;

public enum StatutDemande {
    DEMANDE,
    ACCEPTEE,
    DIAGNOSTIC,
    EN_REPARATION,
    TERMINEE,
    LIVREE;

    public boolean peutTransitionnerVers(StatutDemande cible) {
        return cible != null && cible.ordinal() == this.ordinal() + 1;
    }

    /**
     * Une intervention ne peut être planifiée qu'une fois la demande acceptée,
     * et tant qu'elle n'est pas livrée.
     */
    public boolean autorisePlanificationIntervention() {
        return this != DEMANDE && this != LIVREE;
    }
}
