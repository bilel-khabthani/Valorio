package com.valorio.recommandation.external.model;

public record ArticleMarcheRef(
        Long id,
        String titre,
        String categorie,
        String etat,
        Double prix,
        String description
) {
}
