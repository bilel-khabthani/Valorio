package com.valorio.recommandation.service;

import com.valorio.recommandation.domain.RouteValorisation;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.dto.ActeurClasse;
import com.valorio.recommandation.dto.ArticleClasse;
import com.valorio.recommandation.dto.ArticleMarche;
import com.valorio.recommandation.dto.AlternativeReco;
import com.valorio.recommandation.dto.CandidatActeur;
import com.valorio.recommandation.dto.ObjetAnalyseInput;
import com.valorio.recommandation.dto.RaisonReco;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import com.valorio.recommandation.util.Geo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class HeuristicRecoProvider {

    public static final Set<String> MATERIAUX_VALORISABLES = Set.of(
            "plastique", "aluminium", "acier", "verre", "bois",
            "papier", "carton", "caoutchouc", "cuivre", "lithium");

    public static final String VERSION_HEURISTIQUE = "heuristique-1.0.0";

    private static final Map<String, Double> BENCHMARKS_CATEGORIE = Map.of(
            "electronique", 450.0,
            "meubles", 180.0,
            "velo & mobilite", 220.0,
            "vetements", 60.0,
            "cuisine", 120.0,
            "autre", 100.0);

    private static final Map<String, Double> FACTEUR_ETAT = Map.of(
            "NEUF", 1.25,
            "EXCELLENT", 1.1,
            "BON", 1.0,
            "ENDOMMAGE", 0.6,
            "HORS_SERVICE", 0.25);

    private static final double BENCHMARK_PAR_DEFAUT = 100.0;
    private static final double FACTEUR_ETAT_PAR_DEFAUT = 1.0;

    private static final Pattern PATRON_AGE = Pattern.compile("\\b(\\d{1,2})\\s*ans?\\b", Pattern.CASE_INSENSITIVE);

    private static final Set<String> MOTS_VIDES = Set.of(
            "le", "la", "les", "de", "des", "du", "un", "une", "et", "en", "au", "aux",
            "a", "pour", "avec", "sur", "dans", "est", "se", "son", "sa", "ses", "qui",
            "que", "plus", "tres", "bien", "the", "and");

    private record Decision(RouteValorisation route, double certitude) {
    }

    public static double tauxRecyclabilite(List<String> materiaux) {
        List<String> uniques = normaliserMateriaux(materiaux);
        if (uniques.isEmpty()) {
            return 0.0;
        }
        long valorisables = uniques.stream().filter(MATERIAUX_VALORISABLES::contains).count();
        return (double) valorisables / uniques.size();
    }

    public RecoValorisationResponse recommanderValorisation(ObjetAnalyseInput objet) {
        double valeur = objet.valeurEstimee() != null ? objet.valeurEstimee() : 0.0;
        double cout = objet.coutReparationEstime() != null ? objet.coutReparationEstime() : 0.0;
        double age = objet.ageAnnees() != null ? objet.ageAnnees() : 0.0;
        double taux = objet.tauxRecyclabilite() != null
                ? objet.tauxRecyclabilite()
                : tauxRecyclabilite(objet.materiaux());
        String etat = etatNormalise(objet.etat());
        double coutRatio = valeur > 0 ? cout / valeur : (cout > 0 ? 1.0 : 0.0);

        Decision decision = choisirRoute(etat, coutRatio, taux, valeur, age);
        Map<RouteValorisation, Double> probabilites =
                repartirProbabilites(decision, coutRatio, taux, valeur);

        List<AlternativeReco> alternatives = probabilites.entrySet().stream()
                .filter(e -> e.getKey() != decision.route())
                .sorted(Map.Entry.<RouteValorisation, Double>comparingByValue().reversed())
                .map(e -> new AlternativeReco(e.getKey(), e.getValue()))
                .toList();

        return new RecoValorisationResponse(
                null,
                decision.route(),
                probabilites.get(decision.route()),
                alternatives,
                construireRaisons(objet, decision, coutRatio, taux, valeur, cout, age),
                arrondi(0.5 + 0.5 * decision.certitude()),
                SourcePrediction.HEURISTIQUE,
                VERSION_HEURISTIQUE);
    }

    public List<ActeurClasse> classementActeurs(List<CandidatActeur> candidats,
                                                ObjetAnalyseInput objet,
                                                Double latitude,
                                                Double longitude) {
        String categorieId = objet != null && objet.categorieId() != null
                ? String.valueOf(objet.categorieId()) : null;
        String categorieNom = objet != null ? objet.categorie() : null;

        record Poids(CandidatActeur candidat, double score, double distance, boolean positionConnue, boolean match) {
        }

        List<Poids> ponderes = new ArrayList<>();
        for (CandidatActeur candidat : candidats) {
            double noteNorm = candidat.noteMoyenne() != null
                    ? clamp(candidat.noteMoyenne() / 5.0, 0, 1) : 0.0;
            boolean match = matchCategorie(candidat.categories(), categorieId, categorieNom);
            Double distance = distanceKm(latitude, longitude, candidat.latitude(), candidat.longitude());
            double facteurDistance = distance == null ? 0.5 : 1 - Math.min(distance / 100.0, 1);
            double disponible = Boolean.TRUE.equals(candidat.disponible()) ? 1.0 : 0.0;
            double score = clamp(0.45 * noteNorm
                    + 0.25 * (match ? 1.0 : 0.0)
                    + 0.2 * facteurDistance
                    + 0.1 * disponible, 0, 1);
            ponderes.add(new Poids(candidat, score, distance == null ? 0.0 : distance, distance != null, match));
        }

        ponderes.sort(Comparator.comparingDouble((Poids p) -> p.score()).reversed()
                .thenComparing(p -> p.candidat().id() == null ? Long.MAX_VALUE : p.candidat().id()));

        List<ActeurClasse> classement = new ArrayList<>();
        for (Poids p : ponderes) {
            double delai = p.positionConnue() ? 1 + p.distance() / 80.0 : 1.0;
            CandidatActeur c = p.candidat();
            List<String> raisons = new ArrayList<>();
            raisons.add(c.noteMoyenne() != null
                    ? String.format(Locale.ROOT, "Note moyenne %.1f/5 sur %d avis",
                    c.noteMoyenne(), c.nbAvis() == null ? 0 : c.nbAvis())
                    : "Note inconnue");
            if (p.match()) {
                raisons.add("Catégorie compatible avec la demande");
            }
            raisons.add(p.positionConnue()
                    ? String.format(Locale.ROOT, "À %.1f km — délai estimé %.1f jour(s)",
                    p.distance(), delai)
                    : "Localisation inconnue — délai estimé 1 jour");
            raisons.add(Boolean.TRUE.equals(c.disponible()) ? "Acteur disponible" : "Acteur indisponible");

            double score = arrondi(p.score());
            classement.add(new ActeurClasse(c.id(), score, score, arrondi(delai),
                    arrondi(p.distance()), List.copyOf(raisons)));
        }
        return classement;
    }

    public List<ArticleClasse> classementMarche(ArticleMarche requete, List<ArticleMarche> candidats) {
        Map<String, Integer> vecteurRequete = vecteur(requete);
        List<ArticleClasse> classes = new ArrayList<>();

        for (ArticleMarche candidat : candidats) {
            if (requete.id() != null && requete.id().equals(candidat.id())) {
                continue;
            }
            double similarite = cosine(vecteurRequete, vecteur(candidat));
            double age = ageDepuisDescription(candidat.description());
            double prixRecommande = prixFaire(candidat.categorie(), candidat.etat(), age);
            double prix = candidat.prix() != null ? candidat.prix() : 0.0;
            double ratio = prixRecommande > 0 ? prix / prixRecommande : 1.0;

            String equite;
            double scoreEquite;
            if (ratio <= 0.7) {
                equite = "EXCELLENT_DEAL";
                scoreEquite = 1.0;
            } else if (ratio <= 1.25) {
                equite = "FAIR_MARKET";
                scoreEquite = 0.7;
            } else {
                equite = "OVERPRICED";
                scoreEquite = 0.35;
            }
            double scoreDeal = clamp(0.6 * similarite + 0.4 * scoreEquite, 0, 1);

            List<String> raisons = List.of(
                    String.format(Locale.ROOT, "Similarité de contenu de %.0f %% avec la requête",
                            similarite * 100),
                    String.format(Locale.ROOT, "Prix %.0f € vs prix recommandé %.0f €",
                            prix, prixRecommande),
                    libelleEquite(equite, ratio));

            classes.add(new ArticleClasse(candidat.id(), candidat.titre(), arrondi(similarite),
                    arrondi(scoreDeal), prix, arrondi(prixRecommande), equite, raisons));
        }

        classes.sort(Comparator.comparingDouble((ArticleClasse a) -> a.scoreDeal()).reversed()
                .thenComparing(ArticleClasse::similarite, Comparator.reverseOrder())
                .thenComparing(a -> a.articleId() == null ? Long.MAX_VALUE : a.articleId()));
        return classes;
    }

    public double prixFaire(String categorie, String etat, double ageAnnees) {
        String cle = categorie != null ? categorie.trim().toLowerCase(Locale.ROOT) : "autre";
        double benchmark = BENCHMARKS_CATEGORIE.getOrDefault(cle, BENCHMARK_PAR_DEFAUT);
        double facteurEtat = FACTEUR_ETAT.getOrDefault(etatNormalise(etat), FACTEUR_ETAT_PAR_DEFAUT);
        double facteurAge = 1 - Math.min(0.5, 0.07 * Math.max(ageAnnees, 0));
        return arrondi(benchmark * facteurEtat * facteurAge);
    }

    private Decision choisirRoute(String etat, double coutRatio, double taux, double valeur, double age) {
        boolean eligibiliteRevente = ("NEUF".equals(etat) || "EXCELLENT".equals(etat))
                && age <= 2 && valeur >= 150;
        if (eligibiliteRevente) {
            double certitudeValeur = clamp((valeur - 150) / 350.0, 0, 1);
            double certitudeAge = age <= 0 ? 1.0 : clamp((2 - age) / 2.0, 0, 1);
            return new Decision(RouteValorisation.REVENTE,
                    0.45 + 0.55 * (0.6 * certitudeValeur + 0.4 * certitudeAge));
        }

        switch (etat) {
            case "ENDOMMAGE":
                if (coutRatio <= 0.5) {
                    return new Decision(RouteValorisation.REPARATION,
                            clamp((0.5 - coutRatio) / 0.5, 0.1, 1.0));
                }
                if (taux >= 0.5) {
                    if (coutRatio <= 0.8) {
                        return new Decision(RouteValorisation.REPARATION,
                                clamp((0.8 - coutRatio) / 0.3, 0.1, 0.9));
                    }
                    return new Decision(RouteValorisation.RECYCLAGE,
                            clamp(Math.min((coutRatio - 0.8) / 0.4, (taux - 0.5) / 0.5), 0.1, 0.9));
                }
                if (coutRatio > 0.8) {
                    return new Decision(RouteValorisation.DON,
                            clamp((coutRatio - 0.8) / 0.4, 0.1, 0.9));
                }
                return new Decision(RouteValorisation.REPARATION,
                        clamp((0.8 - coutRatio) / 0.3, 0.1, 0.8));
            case "HORS_SERVICE":
                if (taux >= 0.5) {
                    return new Decision(RouteValorisation.RECYCLAGE,
                            clamp((taux - 0.5) / 0.5, 0.1, 1.0));
                }
                return new Decision(RouteValorisation.DON,
                        clamp((0.5 - taux) / 0.5, 0.1, 1.0));
            case "BON":
                if (valeur >= 80) {
                    return new Decision(RouteValorisation.REVENTE,
                            clamp((valeur - 80) / 220.0, 0.1, 0.9));
                }
                return new Decision(RouteValorisation.DON,
                        clamp((80 - valeur) / 80.0, 0.1, 1.0));
            default:
                return new Decision(RouteValorisation.DON, 0.45);
        }
    }

    private Map<RouteValorisation, Double> repartirProbabilites(Decision decision, double coutRatio,
                                                                double taux, double valeur) {
        Map<RouteValorisation, Double> probabilites = new EnumMap<>(RouteValorisation.class);
        double gagnante = arrondi(0.62 + 0.16 * clamp(decision.certitude(), 0, 1));
        probabilites.put(decision.route(), gagnante);

        Map<RouteValorisation, Double> poids = new EnumMap<>(RouteValorisation.class);
        double total = 0;
        for (RouteValorisation route : RouteValorisation.values()) {
            if (route == decision.route()) {
                continue;
            }
            double poidsRoute = affinite(route, coutRatio, taux, valeur) + 0.05;
            poids.put(route, poidsRoute);
            total += poidsRoute;
        }
        double reste = arrondi(1 - gagnante);
        double somme = total;
        poids.forEach((route, poidsRoute) -> probabilites.put(route, arrondi(reste * poidsRoute / somme)));
        return probabilites;
    }

    private double affinite(RouteValorisation route, double coutRatio, double taux, double valeur) {
        return switch (route) {
            case REPARATION -> clamp(1 - Math.min(coutRatio, 1), 0, 1);
            case RECYCLAGE -> clamp(taux, 0, 1);
            case DON -> 0.5;
            case REVENTE -> clamp((valeur - 100) / 400.0, 0, 1);
        };
    }

    private List<RaisonReco> construireRaisons(ObjetAnalyseInput objet, Decision decision,
                                               double coutRatio, double taux, double valeur,
                                               double cout, double age) {
        RouteValorisation route = decision.route();

        double contributionCout;
        if (route == RouteValorisation.REPARATION) {
            contributionCout = clamp(1 - coutRatio, 0, 1);
        } else if (route == RouteValorisation.REVENTE) {
            contributionCout = -clamp(coutRatio, 0, 1);
        } else {
            contributionCout = clamp(coutRatio, 0, 1);
        }
        RaisonReco raisonCout = new RaisonReco(
                "coutRatio",
                String.format(Locale.ROOT, "%.2f", coutRatio),
                arrondi(contributionCout),
                String.format(Locale.ROOT,
                        "Coût de réparation (%.0f €) équivalent à %.0f %% de la valeur estimée (%.0f €)",
                        cout, valeur > 0 ? 100 * coutRatio : 0.0, valeur));

        double contributionEtat = supportEtat(route, etatNormalise(objet.etat()));
        RaisonReco raisonEtat = new RaisonReco(
                "etat",
                etatNormalise(objet.etat()),
                arrondi(contributionEtat),
                String.format(Locale.ROOT, "État de l'objet : %s, âge de %.0f an(s), valeur estimée à %.0f €",
                        etatNormalise(objet.etat()), age, valeur));

        double contributionTaux = route == RouteValorisation.RECYCLAGE ? clamp(taux, 0, 1)
                : route == RouteValorisation.DON ? clamp(1 - taux, 0, 1) * 0.6 : 0.3;
        RaisonReco raisonTaux = new RaisonReco(
                "tauxRecyclabilite",
                String.format(Locale.ROOT, "%.2f", taux),
                arrondi(contributionTaux),
                raisonTauxLibelle(objet.materiaux(), taux));

        return List.of(raisonCout, raisonEtat, raisonTaux);
    }

    private String raisonTauxLibelle(List<String> materiaux, double taux) {
        List<String> uniques = normaliserMateriaux(materiaux);
        if (uniques.isEmpty()) {
            return "Taux de recyclabilité de 0 % : aucun matériau identifié sur l'objet";
        }
        long valorisables = uniques.stream().filter(MATERIAUX_VALORISABLES::contains).count();
        return String.format(Locale.ROOT,
                "Taux de recyclabilité de %.0f %% : %d matériaux valorisables sur %d",
                taux * 100, valorisables, uniques.size());
    }

    private double supportEtat(RouteValorisation route, String etat) {
        return switch (route) {
            case REVENTE -> switch (etat) {
                case "NEUF" -> 1.0;
                case "EXCELLENT" -> 0.9;
                case "BON" -> 0.5;
                case "ENDOMMAGE" -> -0.4;
                default -> -0.8;
            };
            case REPARATION -> switch (etat) {
                case "ENDOMMAGE" -> 1.0;
                case "BON" -> 0.3;
                case "HORS_SERVICE" -> -0.6;
                default -> 0.2;
            };
            case RECYCLAGE -> switch (etat) {
                case "HORS_SERVICE" -> 1.0;
                case "ENDOMMAGE" -> 0.4;
                default -> -0.3;
            };
            case DON -> switch (etat) {
                case "HORS_SERVICE" -> 0.7;
                case "BON" -> 0.6;
                default -> 0.4;
            };
        };
    }

    private boolean matchCategorie(String categories, String categorieId, String categorieNom) {
        if (categories == null || categories.isBlank()) {
            return false;
        }
        for (String token : categories.split(",")) {
            String nettoye = token.trim();
            if (categorieId != null && nettoye.equals(categorieId)) {
                return true;
            }
            if (categorieNom != null && nettoye.equalsIgnoreCase(categorieNom)) {
                return true;
            }
        }
        return false;
    }

    private Double distanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        return Geo.distanceKm(lat1, lon1, lat2, lon2);
    }

    private Map<String, Integer> vecteur(ArticleMarche article) {
        Map<String, Integer> vecteur = new HashMap<>();
        if (article == null) {
            return vecteur;
        }
        String texte = String.join(" ",
                article.titre() == null ? "" : article.titre(),
                article.titre() == null ? "" : article.titre(),
                article.description() == null ? "" : article.description(),
                article.categorie() == null ? "" : article.categorie(),
                article.etat() == null ? "" : article.etat());
        for (String token : texte.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (token.isBlank() || MOTS_VIDES.contains(token)) {
                continue;
            }
            vecteur.merge(token, 1, Integer::sum);
        }
        return vecteur;
    }

    private double cosine(Map<String, Integer> gauche, Map<String, Integer> droite) {
        if (gauche.isEmpty() || droite.isEmpty()) {
            return 0.0;
        }
        double produit = 0;
        double normeGauche = 0;
        for (Map.Entry<String, Integer> e : gauche.entrySet()) {
            normeGauche += (double) e.getValue() * e.getValue();
            Integer occurrence = droite.get(e.getKey());
            if (occurrence != null) {
                produit += (double) e.getValue() * occurrence;
            }
        }
        double normeDroite = 0;
        for (Integer occurrence : droite.values()) {
            normeDroite += (double) occurrence * occurrence;
        }
        if (normeGauche == 0 || normeDroite == 0) {
            return 0.0;
        }
        return clamp(produit / (Math.sqrt(normeGauche) * Math.sqrt(normeDroite)), 0, 1);
    }

    private double ageDepuisDescription(String description) {
        if (description == null) {
            return 0;
        }
        Matcher matcher = PATRON_AGE.matcher(description);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String libelleEquite(String equite, double ratio) {
        return switch (equite) {
            case "EXCELLENT_DEAL" -> String.format(Locale.ROOT,
                    "Prix inférieur de %.0f %% au prix recommandé : affaire exceptionnelle",
                    (1 - ratio) * 100);
            case "OVERPRICED" -> String.format(Locale.ROOT,
                    "Prix supérieur de %.0f %% au prix recommandé : surprix",
                    (ratio - 1) * 100);
            default -> "Prix dans la norme du marché";
        };
    }

    private static List<String> normaliserMateriaux(List<String> materiaux) {
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

    private static String etatNormalise(String etat) {
        return etat == null ? "INCONNU" : etat.trim().toUpperCase(Locale.ROOT);
    }

    private static double clamp(double valeur, double min, double max) {
        return Math.max(min, Math.min(max, valeur));
    }

    private static double arrondi(double valeur) {
        return Math.round(valeur * 10000.0) / 10000.0;
    }
}
