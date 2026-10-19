package com.valorio.reparation.ai.client;

import com.valorio.reparation.ai.model.DiagnosticRequest;
import com.valorio.reparation.ai.model.DiagnosticResponse;
import com.valorio.reparation.ai.model.DiagnosticResponse.DevisEstime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Moteur de diagnostic heuristique (hors-ligne, déterministe). Il détecte la
 * catégorie de panne par correspondance de mots-clés et en déduit une gravité,
 * une explication et une fourchette de devis. Sert de repli garanti lorsque le
 * fournisseur IA (Gemini) est indisponible ou non configuré.
 */
@Component
public class HeuristicDiagnosticClient implements DiagnosticAiClient {

    public static final String SOURCE = "heuristique";
    private static final String DEVISES = "DT";

    private record Profil(String categorie, List<String> motsCles, BigDecimal prixBase, String cause) {
    }

    private static final List<Profil> PROFILS = List.of(
            new Profil("ECRAN", List.of("ecran", "affichage", "dalle", "tactile", "afficheur", "pixel", "lcd"),
                    bd(250), "Défaillance de la dalle d'affichage ou de la nappe écran"),
            new Profil("BATTERIE", List.of("batterie", "autonomie", "ne charge", "charge plus", "decharge", "gonfle"),
                    bd(120), "Usure ou gonflement de la batterie"),
            new Profil("CARTE_MERE", List.of("carte mere", "circuit", "oxyd", "court circuit", "composant",
                    "condensateur", "connecteur", "soudure"),
                    bd(320), "Défaut sur la carte mère (composant, connecteur ou soudure)"),
            new Profil("ALIMENTATION", List.of("alimentation", "chargeur", "prise", "ne s allume", "ne demarre",
                    "demarre pas", "allumage", "fusible"),
                    bd(90), "Défaut d'alimentation (chargeur, prise ou étage d'entrée)"),
            new Profil("CLAVIER", List.of("clavier", "touche", "bouton", "saisie"),
                    bd(80), "Défaut du clavier ou de la nappe de saisie"),
            new Profil("DISQUE", List.of("disque", "stockage", "hdd", "ssd", "donnees", "lent", "boot", "demarrage"),
                    bd(180), "Usure ou défaillance du disque de stockage (HDD/SSD)"),
            new Profil("REFROIDISSEMENT", List.of("surchauffe", "chauffe", "ventilateur", "refroidissement", "bruit",
                    "temperature"),
                    bd(110), "Système de refroidissement encrassé ou défaillant (ventilateur/pâte thermique)"),
            new Profil("MOTEUR_ELECTRIQUE", List.of("moteur", "electrique", "bobinage", "rotor", "stator", "grince"),
                    bd(200), "Défaut du moteur électrique (bobinage, roulements)"),
            new Profil("MENUISERIE", List.of("bois", "menuiserie", "meuble", "table", "chaise", "pied", "vernis",
                    "fissure", "plan de travail"),
                    bd(150), "Dégradation structurelle du bois ou de la menuiserie"),
            new Profil("FREINAGE", List.of("frein", "freinage", "plaquette", "patin", "cable de frein", "etrier"),
                    bd(60), "Usure ou mauvais réglage des organes de freinage"),
            new Profil("TRANSMISSION", List.of("chaine", "transmission", "derailleur", "pignon", "vitesse", "pedalier",
                    "courroie"),
                    bd(70), "Usure ou déréglage de la transmission (chaîne, dérailleur, courroie)")
    );

    private static final List<String> MOTS_CRITIQUES = List.of(
            "explose", "explos", "fume", "brule", "incendie", "inond", "court circuit", "ne s allume plus",
            "totalement mort", "casse en deux");
    private static final List<String> MOTS_ELEVES = List.of(
            "casse", "grave", "important", "ne fonctionne plus", "ne marche plus", "bloque", "gonfle", "fissure");

    @Override
    public String nom() {
        return SOURCE;
    }

    @Override
    public DiagnosticResponse diagnostiquer(DiagnosticRequest request) {
        String texte = normaliser(String.join(" ",
                nul(request.intitule()), nul(request.etat()), nul(request.descriptionPanne())));

        Profil profil = meilleurProfil(texte);
        String categorie = profil != null ? profil.categorie() : "AUTRE";
        String causePrincipale = profil != null
                ? profil.cause()
                : "Cause indéterminée à partir de la description fournie";
        BigDecimal prixBase = profil != null ? profil.prixBase() : bd(100);

        String gravite = gravite(texte, profil);
        List<String> causes = new ArrayList<>();
        causes.add(causePrincipale);
        causes.addAll(causesSecondaires(categorie));

        List<String> avertissements = new ArrayList<>();
        avertissements.add("Estimation indicative générée hors-ligne, à confirmer par un réparateur.");
        if (profil == null) {
            avertissements.add("Aucun mot-clé technique reconnu : diagnostic générique.");
        }

        DiagnosticResponse.DevisEstime devis = devis(prixBase, gravite);

        return new DiagnosticResponse(
                categorie,
                gravite,
                explication(categorie, gravite, causePrincipale),
                causes,
                devis,
                List.of(),
                confiance(texte, profil),
                SOURCE,
                avertissements);
    }

    private Profil meilleurProfil(String texte) {
        Profil meilleur = null;
        int meilleurScore = 0;
        for (Profil profil : PROFILS) {
            int score = 0;
            for (String mot : profil.motsCles()) {
                if (texte.contains(mot)) {
                    score++;
                }
            }
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleur = profil;
            }
        }
        return meilleur;
    }

    private String gravite(String texte, Profil profil) {
        if (contient(texte, MOTS_CRITIQUES)) {
            return "CRITIQUE";
        }
        if (contient(texte, MOTS_ELEVES)) {
            return "ELEVEE";
        }
        if (profil != null && List.of("CARTE_MERE", "ECRAN", "MOTEUR_ELECTRIQUE").contains(profil.categorie())) {
            return "ELEVEE";
        }
        return profil != null ? "MOYENNE" : "FAIBLE";
    }

    private List<String> causesSecondaires(String categorie) {
        return switch (categorie) {
            case "ALIMENTATION" -> List.of("Chargeur ou câble défectueux");
            case "ECRAN" -> List.of("Nappe vidéo mal connectée");
            case "REFROIDISSEMENT" -> List.of("Pâte thermique sèche ou ventilateur bloqué");
            case "FREINAGE" -> List.of("Réglage des étriers et tension des câbles");
            case "TRANSMISSION" -> List.of("Chaîne détendue ou pignons usés");
            default -> List.of();
        };
    }

    private String explication(String categorie, String gravite, String cause) {
        return "Catégorie détectée : " + categorie + ". Gravité estimée : " + gravite + ". " + cause + ".";
    }

    private DevisEstime devis(BigDecimal prixBase, String gravite) {
        double facteur = switch (gravite) {
            case "CRITIQUE" -> 1.6;
            case "ELEVEE" -> 1.3;
            case "MOYENNE" -> 1.0;
            default -> 0.7;
        };
        BigDecimal min = arrondi5(prixBase.multiply(BigDecimal.valueOf(facteur * 0.8)));
        BigDecimal max = arrondi5(prixBase.multiply(BigDecimal.valueOf(facteur * 1.3)));
        int delai = switch (gravite) {
            case "CRITIQUE" -> 5;
            case "ELEVEE" -> 3;
            case "MOYENNE" -> 2;
            default -> 1;
        };
        return new DevisEstime(min, max, delai, DEVISES);
    }

    private double confiance(String texte, Profil profil) {
        if (profil == null) {
            return 0.35;
        }
        int correspondances = 0;
        for (String mot : profil.motsCles()) {
            if (texte.contains(mot)) {
                correspondances++;
            }
        }
        double valeur = 0.5 + 0.12 * correspondances;
        return Math.min(0.9, Math.round(valeur * 100) / 100.0);
    }

    private static BigDecimal arrondi5(BigDecimal valeur) {
        return valeur.divide(BigDecimal.valueOf(5), 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(5));
    }

    private static boolean contient(String texte, List<String> mots) {
        return mots.stream().anyMatch(texte::contains);
    }

    private static String normaliser(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return "";
        }
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String nul(String valeur) {
        return valeur == null ? "" : valeur;
    }

    private static BigDecimal bd(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP);
    }
}
