package com.valorio.reparation.ai.service;

import com.valorio.reparation.ai.client.GeminiDiagnosticClient;
import com.valorio.reparation.ai.client.HeuristicDiagnosticClient;
import com.valorio.reparation.ai.model.DiagnosticRequest;
import com.valorio.reparation.ai.model.DiagnosticResponse;
import com.valorio.reparation.ai.model.DiagnosticResponse.ReparateurRecommande;
import com.valorio.reparation.domain.Reparateur;
import com.valorio.reparation.external.ObjetPort;
import com.valorio.reparation.external.model.ObjetRef;
import com.valorio.reparation.repo.ReparationRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Orchestre l'assistant IA de diagnostic : sélection du fournisseur (Gemini ou
 * heuristique), enrichissement depuis l'objet réel, recommandation des
 * réparateurs et mise en cache court terme des résultats.
 */
@Service
public class DiagnosticService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticService.class);
    private static final int TAILLE_CACHE_MAX = 200;

    private final GeminiDiagnosticClient gemini;
    private final HeuristicDiagnosticClient heuristique;
    private final ReparationRepository repository;
    private final ObjetPort objetPort;
    private final String provider;

    private final Map<String, DiagnosticResponse> cache = new ConcurrentHashMap<>();

    public DiagnosticService(
            GeminiDiagnosticClient gemini,
            HeuristicDiagnosticClient heuristique,
            ReparationRepository repository,
            ObjetPort objetPort,
            @Value("${valorio.ai.provider:gemini}") String provider) {
        this.gemini = gemini;
        this.heuristique = heuristique;
        this.repository = repository;
        this.objetPort = objetPort;
        this.provider = provider == null ? "gemini" : provider.trim();
    }

    public DiagnosticResponse diagnostiquer(DiagnosticRequest request) {
        DiagnosticRequest enrichie = enrichir(request);
        String cle = cle(enrichie);
        DiagnosticResponse enCache = cache.get(cle);
        if (enCache != null) {
            return enCache;
        }

        DiagnosticResponse reponse = avecReparateurs(analyser(enrichie), enrichie.ville());
        if (cache.size() < TAILLE_CACHE_MAX) {
            cache.put(cle, reponse);
        }
        return reponse;
    }

    public String fournisseurActif() {
        return useGemini() ? GeminiDiagnosticClient.SOURCE + ":" + gemini.modele() : HeuristicDiagnosticClient.SOURCE;
    }

    private boolean useGemini() {
        return "gemini".equalsIgnoreCase(provider) && gemini.isConfigured();
    }

    private DiagnosticResponse analyser(DiagnosticRequest request) {
        if (useGemini()) {
            try {
                return gemini.diagnostiquer(request);
            } catch (RuntimeException e) {
                log.warn("Diagnostic Gemini indisponible, repli sur le moteur heuristique : {}", e.getMessage());
                return replier(request, "Fournisseur Gemini indisponible : repli sur le moteur local.");
            }
        }
        if (!provider.equalsIgnoreCase(HeuristicDiagnosticClient.SOURCE) && !useGemini()) {
            log.debug("Fournisseur IA '{}' non exploitable pour le diagnostic, moteur heuristique utilisé.", provider);
        }
        return heuristique.diagnostiquer(request);
    }

    private DiagnosticResponse replier(DiagnosticRequest request, String avertissement) {
        DiagnosticResponse base = heuristique.diagnostiquer(request);
        List<String> avertissements = new ArrayList<>(base.avertissements());
        avertissements.add(avertissement);
        return new DiagnosticResponse(
                base.categoriePanne(),
                base.gravite(),
                base.diagnostic(),
                base.causesProbables(),
                base.devisEstime(),
                base.reparateursRecommandes(),
                base.confiance(),
                base.source(),
                avertissements);
    }

    private DiagnosticRequest enrichir(DiagnosticRequest request) {
        if (request.objetId() == null
                || (request.intitule() != null && request.etat() != null)) {
            return request;
        }
        return objetPort.findById(request.objetId())
                .map(objet -> new DiagnosticRequest(
                        request.objetId(),
                        request.descriptionPanne(),
                        request.intitule() != null ? request.intitule() : objet.intitule(),
                        request.etat() != null ? request.etat() : objet.etat(),
                        request.ageAnnees(),
                        request.ville() != null ? request.ville() : objet.ville()))
                .orElse(request);
    }

    private DiagnosticResponse avecReparateurs(DiagnosticResponse reponse, String ville) {
        List<Reparateur> candidats = repository.rechercherReparateurs(null, null, null);
        List<ReparateurRecommande> classement = new ArrayList<>();
        for (Reparateur reparateur : candidats) {
            double score = score(reponse.categoriePanne(), reparateur, ville);
            classement.add(new ReparateurRecommande(reparateur.id(), score, reparateur.ville()));
        }
        classement.sort(Comparator.comparingDouble(ReparateurRecommande::score).reversed());
        List<ReparateurRecommande> top = classement.stream().limit(3).toList();

        return new DiagnosticResponse(
                reponse.categoriePanne(),
                reponse.gravite(),
                reponse.diagnostic(),
                reponse.causesProbables(),
                reponse.devisEstime(),
                top,
                reponse.confiance(),
                reponse.source(),
                reponse.avertissements());
    }

    private double score(String categorie, Reparateur reparateur, String ville) {
        double score = 30.0;
        score += reparateur.noteMoyenne() * 8.0;
        String mots = (nul(reparateur.categories()) + " " + nul(reparateur.competences())).toLowerCase(Locale.ROOT);
        if (contientMot(mots, categorie)) {
            score += 20.0;
        }
        if (ville != null && !ville.isBlank() && reparateur.ville() != null
                && reparateur.ville().equalsIgnoreCase(ville.trim())) {
            score += 10.0;
        }
        return Math.min(100.0, Math.round(score * 10) / 10.0);
    }

    private boolean contientMot(String mots, String categorie) {
        for (String alias : alias(categorie)) {
            if (mots.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private List<String> alias(String categorie) {
        return switch (categorie) {
            case "CARTE_MERE" -> List.of("carte", "electronique");
            case "MOTEUR_ELECTRIQUE" -> List.of("moteur", "electrique", "electronique");
            case "ALIMENTATION" -> List.of("alimentation", "electronique");
            case "TRANSMISSION" -> List.of("velo", "chaine", "transmission");
            case "FREINAGE" -> List.of("frein", "velo");
            case "MENUISERIE" -> List.of("bois", "menuiserie", "meuble");
            case "REFROIDISSEMENT" -> List.of("refroidissement", "ventilateur");
            case "DISQUE", "CLAVIER", "ECRAN", "BATTERIE" -> List.of(categorie.toLowerCase(Locale.ROOT));
            default -> List.of(categorie.toLowerCase(Locale.ROOT));
        };
    }

    private String cle(DiagnosticRequest request) {
        return fournisseurActif() + "|" + nul(request.intitule()) + "|" + nul(request.descriptionPanne());
    }

    private static String nul(String valeur) {
        return valeur == null ? "" : valeur;
    }
}
