package com.valorio.recyclage.domain;

import com.valorio.recyclage.external.model.ObjetRef;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RegleRecyclabilite {

    public static final Set<String> MATERIAUX_VALORISABLES = Set.of(
            "plastique", "aluminium", "acier", "verre", "bois",
            "papier", "carton", "caoutchouc", "cuivre", "lithium");

    private RegleRecyclabilite() {
    }

    public static AnalyseRecyclabilite analyser(ObjetRef objet) {
        List<String> materiaux = normaliser(objet.materiaux());
        List<String> motifs = new ArrayList<>();

        if (materiaux.isEmpty()) {
            motifs.add("Aucun matériau identifié sur l'objet");
            return new AnalyseRecyclabilite(objet.id(), VerdictRecyclabilite.NON, List.of(), motifs, List.of());
        }

        List<String> valorisables = materiaux.stream().filter(MATERIAUX_VALORISABLES::contains).toList();
        List<String> nonValorisables = materiaux.stream().filter(m -> !MATERIAUX_VALORISABLES.contains(m)).toList();

        VerdictRecyclabilite verdict;
        if (nonValorisables.isEmpty()) {
            verdict = VerdictRecyclabilite.OUI;
            motifs.add("Tous les matériaux sont valorisables");
        } else if (valorisables.isEmpty()) {
            verdict = VerdictRecyclabilite.NON;
            motifs.add("Aucun matériau valorisable : " + String.join(", ", nonValorisables));
        } else {
            verdict = VerdictRecyclabilite.PARTIEL;
            motifs.add("Matériaux valorisables : " + String.join(", ", valorisables));
            motifs.add("Matériaux non pris en charge : " + String.join(", ", nonValorisables));
        }

        return new AnalyseRecyclabilite(objet.id(), verdict, materiaux, motifs, List.of());
    }

    private static List<String> normaliser(List<String> materiaux) {
        if (materiaux == null) {
            return List.of();
        }
        LinkedHashSet<String> uniques = new LinkedHashSet<>();
        for (String materiau : materiaux) {
            if (materiau != null && !materiau.isBlank()) {
                uniques.add(materiau.trim().toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(uniques);
    }
}
