package com.valorio.auth.domain;

/** Cycle de vie d'un signalement : OUVERT -> EN_COURS -> TRAITE / REJETE. */
public enum StatutSignalement {
    OUVERT,
    EN_COURS,
    TRAITE,
    REJETE
}
