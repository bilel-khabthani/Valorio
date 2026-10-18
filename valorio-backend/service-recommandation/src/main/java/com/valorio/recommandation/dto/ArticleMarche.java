package com.valorio.recommandation.dto;

public record ArticleMarche(
        Long id,
        String titre,
        String categorie,
        String etat,
        Double prix,
        String description
) {
}
