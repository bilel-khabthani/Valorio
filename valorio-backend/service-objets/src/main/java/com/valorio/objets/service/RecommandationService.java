package com.valorio.objets.service;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.Recommandation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Module 3 — Recommandation intelligente : évalue l'objet (état, valeur, problèmes,
 * recyclabilité) et recommande la réparation, la revente, le don ou le recyclage,
 * avec une justification explicite.
 */
@Service
public class RecommandationService {

    public static final String REPARER = "REPARER";
    public static final String REVENDRE = "REVENDRE";
    public static final String DONNER = "DONNER";
    public static final String RECYCLER = "RECYCLER";

    /** Matériaux valorisables — même référentiel que le service recyclage. */
    public static final Set<String> MATERIAUX_VALORISABLES = Set.of(
            "plastique", "aluminium", "acier", "verre", "bois",
            "papier", "carton", "caoutchouc", "cuivre", "lithium");

    private static final double SEUIL_VALEUR = 100.0;
    private static final double PART_COUT_REPARATION = 0.3;

    public Recommandation recommander(Objet objet, DiagnosticObjet diagnostic) {
        List<String> problemes = diagnostic == null ? List.of() : diagnostic.problemes();
        Double valeur = objet.valeurEstimee() == null ? 0.0 : objet.valeurEstimee();
        List<String> materiaux = normaliser(objet.materiaux());

        return switch (objet.etat()) {
            case NEUF -> new Recommandation(
                    REVENDRE,
                    "Objet en excellent état (NEUF) : aucune réparation nécessaire, la revente est l'action la plus rentable.",
                    null, null, null);
            case BON -> valeur >= SEUIL_VALEUR
                    ? new Recommandation(
                            REVENDRE,
                            "L'objet est fonctionnel et en bon état ; la demande du marché justifie une revente ("
                                    + valeur + " DT estimés).",
                            null, null, null)
                    : new Recommandation(
                            DONNER,
                            "Valeur marchande faible (" + valeur
                                    + " DT) mais l'objet est encore utile à quelqu'un : le don est l'action pertinente.",
                            null, null, null);
            case ENDOMMAGE -> recommanderEndomamage(valeur, problemes, materiaux);
            case HORS_SERVICE -> {
                List<String> valorisables = materiaux.stream().filter(MATERIAUX_VALORISABLES::contains).toList();
                if (valorisables.isEmpty()) {
                    yield new Recommandation(
                            RECYCLER,
                            "Objet ni réparable ni revendable et aucun matériau valorisable identifié : mise au rebut organisée.",
                            null, null, null);
                }
                yield new Recommandation(
                        RECYCLER,
                        "L'objet n'est ni réparable de façon rentable ni revendable ; ses matériaux valorisables ("
                                + String.join(", ", valorisables) + ") peuvent être recyclés.",
                        null, null, null);
            }
        };
    }

    private Recommandation recommanderEndomamage(double valeur, List<String> problemes, List<String> materiaux) {
        double cout = arrondi(Math.max(valeur * PART_COUT_REPARATION, 0.0));
        double valeurApres = arrondi(valeur);
        double economie = arrondi(valeurApres - cout);

        List<String> valorisables = materiaux.stream().filter(MATERIAUX_VALORISABLES::contains).toList();

        if (valeur >= SEUIL_VALEUR && problemes.size() <= 2 && cout < valeurApres) {
            return new Recommandation(
                    REPARER,
                    "Coût estimé " + cout + " DT très inférieur à la valeur après réparation (" + valeurApres
                            + " DT) : réparation rentable, économie potentielle de " + economie + " DT.",
                    cout, valeurApres, economie);
        }
        if (!valorisables.isEmpty()) {
            return new Recommandation(
                    RECYCLER,
                    "Réparation non rentable pour une valeur estimée de " + valeur
                            + " DT ; matériaux valorisables : " + String.join(", ", valorisables) + ".",
                    cout, valeurApres, economie);
        }
        return new Recommandation(
                DONNER,
                "Valeur faible (" + valeur + " DT) et réparation sans retour sur investissement : le don reste possible tant que l'objet est utilisable.",
                cout, valeurApres, economie);
    }

    private static List<String> normaliser(List<String> materiaux) {
        if (materiaux == null) {
            return List.of();
        }
        Set<String> uniques = new LinkedHashSet<>();
        for (String materiau : materiaux) {
            if (materiau != null && !materiau.isBlank()) {
                uniques.add(materiau.trim().toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(uniques);
    }

    private static double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
