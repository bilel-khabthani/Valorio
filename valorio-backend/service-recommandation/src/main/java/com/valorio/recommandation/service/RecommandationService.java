package com.valorio.recommandation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recommandation.domain.ModeleMetadataEntity;
import com.valorio.recommandation.domain.RecommandationHistorique;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.dto.ActeurClasse;
import com.valorio.recommandation.dto.ActeursRequest;
import com.valorio.recommandation.dto.AlternativeReco;
import com.valorio.recommandation.dto.ArticleClasse;
import com.valorio.recommandation.dto.ArticleMarche;
import com.valorio.recommandation.dto.CandidatActeur;
import com.valorio.recommandation.dto.ExplicationResponse;
import com.valorio.recommandation.dto.FeedbackRequest;
import com.valorio.recommandation.dto.FeedbackResponse;
import com.valorio.recommandation.dto.HistoriqueReco;
import com.valorio.recommandation.dto.MarcheRequest;
import com.valorio.recommandation.dto.ModeleMetadata;
import com.valorio.recommandation.dto.ObjetAnalyseInput;
import com.valorio.recommandation.dto.RaisonReco;
import com.valorio.recommandation.dto.RecoActeursResponse;
import com.valorio.recommandation.dto.RecoMarcheResponse;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import com.valorio.recommandation.dto.ValorisationRequest;
import com.valorio.recommandation.external.ActeurPort;
import com.valorio.recommandation.external.CategoriePort;
import com.valorio.recommandation.external.MarchePort;
import com.valorio.recommandation.external.MarketplaceMarcheClient;
import com.valorio.recommandation.external.ObjetPort;
import com.valorio.recommandation.external.UtilisateurPort;
import com.valorio.recommandation.external.model.ActeurRef;
import com.valorio.recommandation.external.model.ArticleMarcheRef;
import com.valorio.recommandation.external.model.CategorieRef;
import com.valorio.recommandation.external.model.ObjetRef;
import com.valorio.recommandation.external.model.UtilisateurRef;
import com.valorio.recommandation.repo.RecommandationRepository;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommandationService {

    public static final String TYPE_VALORISATION = "VALORISATION";
    public static final String TYPE_ACTEURS = "ACTEURS";
    public static final String TYPE_MARCHE = "MARCHE";

    public static final String VERSION_VALORISATION_ML = "valorisation-gbm-1.0.0";
    public static final String VERSION_ACTEURS_ML = "acteurs-gbr-1.0.0";
    public static final String VERSION_MARCHE_ML = "marche-tfidf-1.0.0";

    private static final double RAYON_DEFAUT_KM = 100.0;
    private static final int TOP_K_DEFAUT = 5;
    private static final int MAX_TRACTION_CHARGE = 1500;

    private final RecommandationRepository repository;
    private final ObjetPort objets;
    private final UtilisateurPort utilisateurs;
    private final CategoriePort categories;
    private final ActeurPort acteurs;
    private final MarchePort marche;
    private final MarketplaceMarcheClient marketplace;
    private final MlClient ml;
    private final HeuristicRecoProvider heuristique;
    private final ExplicationService explication;
    private final ObjectMapper mapper;

    public RecommandationService(RecommandationRepository repository,
                                 ObjetPort objets,
                                 UtilisateurPort utilisateurs,
                                 CategoriePort categories,
                                 ActeurPort acteurs,
                                 MarchePort marche,
                                 MarketplaceMarcheClient marketplace,
                                 MlClient ml,
                                 HeuristicRecoProvider heuristique,
                                 ExplicationService explication,
                                 ObjectMapper mapper) {
        this.repository = repository;
        this.objets = objets;
        this.utilisateurs = utilisateurs;
        this.categories = categories;
        this.acteurs = acteurs;
        this.marche = marche;
        this.marketplace = marketplace;
        this.ml = ml;
        this.heuristique = heuristique;
        this.explication = explication;
        this.mapper = mapper;
    }

    @Transactional
    public RecoValorisationResponse recommanderValorisation(ValorisationRequest request) {
        if (request == null || (request.objetId() == null && request.objet() == null)) {
            throw new IllegalArgumentException("Un objetId ou une description objet inline est obligatoire");
        }
        if (request.objetId() != null && request.objet() != null) {
            throw new IllegalArgumentException("objetId et objet inline sont mutuellement exclusifs");
        }

        Long objetId = request.objetId();
        ObjetAnalyseInput analyse;
        if (objetId != null) {
            analyse = versAnalyse(objetId);
        } else {
            analyse = completer(request.objet());
        }

        RecoValorisationResponse prediction;
        SourcePrediction source;
        String modeleVersion;
        try {
            prediction = ml.predictValorisation(analyse);
            if (prediction == null || prediction.routeRecommandee() == null) {
                throw new MlIndisponibleException("Prediction ML de valorisation illisible");
            }
            source = SourcePrediction.ML;
            modeleVersion = valeurOu(prediction.modeleVersion(), VERSION_VALORISATION_ML);
        } catch (MlIndisponibleException e) {
            prediction = heuristique.recommanderValorisation(analyse);
            source = SourcePrediction.HEURISTIQUE;
            modeleVersion = HeuristicRecoProvider.VERSION_HEURISTIQUE;
        }

        RecoValorisationResponse reponse = new RecoValorisationResponse(
                null,
                prediction.routeRecommandee(),
                prediction.probabilite(),
                prediction.alternatives() == null ? List.of() : prediction.alternatives(),
                prediction.raisons() == null ? List.of() : prediction.raisons(),
                prediction.confidenceScore(),
                source,
                modeleVersion);

        long id = repository.insertHistorique(objetId, TYPE_VALORISATION,
                compact(request), compact(resumeValorisation(reponse)), source.name(), modeleVersion);
        return withId(reponse, id);
    }

    @Transactional
    public RecoActeursResponse recommanderActeurs(ActeursRequest request) {
        if (request == null || request.typeActeur() == null) {
            throw new IllegalArgumentException("Le type d'acteur est obligatoire");
        }

        List<CandidatActeur> candidats = request.candidats() != null && !request.candidats().isEmpty()
                ? request.candidats()
                : acteurs.findAll(request.typeActeur()).stream().map(this::versCandidat).toList();

        Double latitude = request.latitude() != null
                ? request.latitude()
                : request.objet() != null ? request.objet().latitude() : null;
        Double longitude = request.longitude() != null
                ? request.longitude()
                : request.objet() != null ? request.objet().longitude() : null;

        ActeursRequest payload = new ActeursRequest(request.typeActeur(), request.objet(),
                latitude, longitude, request.rayonKm(), candidats);

        List<ActeurClasse> classement;
        SourcePrediction source;
        String modeleVersion;
        try {
            RecoActeursResponse prediction = ml.predictActeurs(payload);
            if (prediction == null || prediction.classement() == null) {
                throw new MlIndisponibleException("Prediction ML d'acteurs illisible");
            }
            classement = prediction.classement();
            source = SourcePrediction.ML;
            modeleVersion = valeurOu(prediction.modeleVersion(), VERSION_ACTEURS_ML);
        } catch (MlIndisponibleException e) {
            classement = heuristique.classementActeurs(candidats, request.objet(), latitude, longitude);
            source = SourcePrediction.HEURISTIQUE;
            modeleVersion = HeuristicRecoProvider.VERSION_HEURISTIQUE;
        }

        RecoActeursResponse reponse = new RecoActeursResponse(request.typeActeur(),
                filtrerParRayon(classement, request.rayonKm()), source, modeleVersion);

        repository.insertHistorique(null, TYPE_ACTEURS,
                compact(payload), compact(resumeActeurs(reponse)), source.name(), modeleVersion);
        return reponse;
    }

    @Transactional
    public RecoMarcheResponse articlesSimilaires(MarcheRequest request) {
        if (request == null || request.requete() == null) {
            throw new IllegalArgumentException("La requête de marché est obligatoire");
        }

        List<ArticleMarche> candidats;
        if (request.candidats() != null && !request.candidats().isEmpty()) {
            candidats = request.candidats();
        } else {
            List<ArticleMarche> enVente = marketplace.findAllDisponibles();
            candidats = !enVente.isEmpty()
                    ? enVente
                    : marche.findAll().stream().map(this::versArticle).toList();
        }
        int topK = request.topK() != null ? request.topK() : TOP_K_DEFAUT;
        MarcheRequest payload = new MarcheRequest(request.requete(), candidats, topK);

        List<ArticleClasse> articles;
        SourcePrediction source;
        String modeleVersion;
        try {
            RecoMarcheResponse prediction = ml.predictMarche(payload);
            if (prediction == null || prediction.articles() == null) {
                throw new MlIndisponibleException("Prediction ML de marché illisible");
            }
            articles = prediction.articles();
            source = SourcePrediction.ML;
            modeleVersion = valeurOu(prediction.modeleVersion(), VERSION_MARCHE_ML);
        } catch (MlIndisponibleException e) {
            articles = heuristique.classementMarche(request.requete(), candidats);
            source = SourcePrediction.HEURISTIQUE;
            modeleVersion = HeuristicRecoProvider.VERSION_HEURISTIQUE;
        }

        RecoMarcheResponse reponse = new RecoMarcheResponse(
                articles.stream().limit(topK).toList(), source, modeleVersion);

        repository.insertHistorique(null, TYPE_MARCHE,
                compact(payload), compact(resumeMarche(reponse)), source.name(), modeleVersion);
        return reponse;
    }

    @Transactional
    public FeedbackResponse enregistrerFeedback(FeedbackRequest request) {
        if (request == null || request.recommandationId() == null || request.utile() == null) {
            throw new IllegalArgumentException("recommandationId et utile sont obligatoires");
        }
        repository.findHistorique(request.recommandationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Recommandation " + request.recommandationId() + " introuvable"));

        long id = repository.insertFeedback(request.recommandationId(), request.utile(),
                request.commentaire());
        return new FeedbackResponse(id, request.recommandationId(), request.utile(), OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public ModeleMetadata metadata() {
        try {
            ModeleMetadata mlMetadata = ml.metadata();
            return new ModeleMetadata(
                    true,
                    SourcePrediction.ML,
                    valeurOu(mlMetadata.versionValorisation(), VERSION_VALORISATION_ML),
                    valeurOu(mlMetadata.versionActeurs(), VERSION_ACTEURS_ML),
                    valeurOu(mlMetadata.versionMarche(), VERSION_MARCHE_ML),
                    mlMetadata.metriques() == null ? Map.of() : mlMetadata.metriques(),
                    mlMetadata.topFeatures() == null ? List.of() : mlMetadata.topFeatures());
        } catch (MlIndisponibleException e) {
            return metadataLocale();
        }
    }

    @Transactional(readOnly = true)
    public List<HistoriqueReco> historique(Long objetId) {
        return repository.listHistorique(objetId).stream()
                .map(this::versHistorique)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExplicationResponse expliquer(long recommandationId) {
        RecommandationHistorique reco = repository.findHistorique(recommandationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Recommandation " + recommandationId + " introuvable"));
        return explication.expliquer(reco);
    }

    private ModeleMetadata metadataLocale() {
        List<ModeleMetadataEntity> lignes = repository.findModeleMetadata();
        if (lignes.isEmpty()) {
            return new ModeleMetadata(false, SourcePrediction.HEURISTIQUE,
                    VERSION_VALORISATION_ML, VERSION_ACTEURS_ML, VERSION_MARCHE_ML,
                    Map.of(), List.of());
        }

        Map<String, Double> metriques = new LinkedHashMap<>();
        List<String> topFeatures = List.of();
        String versionValorisation = VERSION_VALORISATION_ML;
        String versionActeurs = VERSION_ACTEURS_ML;
        String versionMarche = VERSION_MARCHE_ML;

        for (ModeleMetadataEntity ligne : lignes) {
            switch (ligne.nomModele() == null ? "" : ligne.nomModele()) {
                case "valorisation" -> versionValorisation = valeurOu(ligne.version(), VERSION_VALORISATION_ML);
                case "acteurs" -> versionActeurs = valeurOu(ligne.version(), VERSION_ACTEURS_ML);
                case "marche" -> versionMarche = valeurOu(ligne.version(), VERSION_MARCHE_ML);
                default -> {
                }
            }
            metriques.putAll(lireMetriques(ligne.metriques()));
            if (topFeatures.isEmpty() && ligne.topFeatures() != null && !ligne.topFeatures().isBlank()) {
                topFeatures = List.of(ligne.topFeatures().split("\\s*,\\s*"));
            }
        }

        return new ModeleMetadata(false, SourcePrediction.HEURISTIQUE,
                versionValorisation, versionActeurs, versionMarche, metriques, topFeatures);
    }

    private Map<String, Double> lireMetriques(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private ObjetAnalyseInput versAnalyse(long objetId) {
        ObjetRef ref = objets.findById(objetId)
                .orElseThrow(() -> new NoSuchElementException("Objet " + objetId + " introuvable"));
        String nomCategorie = ref.categorieId() == null ? null
                : categories.findById(ref.categorieId()).map(CategorieRef::nom).orElse(null);

        Double latitude = null;
        Double longitude = null;
        if (ref.proprietaireId() != null) {
            Optional<UtilisateurRef> proprietaire = utilisateurs.findById(ref.proprietaireId());
            if (proprietaire.isPresent()) {
                latitude = proprietaire.get().latitude();
                longitude = proprietaire.get().longitude();
            }
        }

        return completer(new ObjetAnalyseInput(
                ref.categorieId(),
                nomCategorie,
                ref.intitule(),
                ref.etat(),
                ref.ageAnnees() != null ? ref.ageAnnees().doubleValue() : null,
                ref.valeurEstimee(),
                0.0,
                ref.ville(),
                latitude,
                longitude,
                ref.materiaux(),
                null));
    }

    private ObjetAnalyseInput completer(ObjetAnalyseInput analyse) {
        double taux = analyse.tauxRecyclabilite() != null
                ? analyse.tauxRecyclabilite()
                : HeuristicRecoProvider.tauxRecyclabilite(analyse.materiaux());
        double cout = analyse.coutReparationEstime() != null ? analyse.coutReparationEstime() : 0.0;
        if (analyse.tauxRecyclabilite() != null && analyse.coutReparationEstime() != null) {
            return analyse;
        }
        return new ObjetAnalyseInput(
                analyse.categorieId(),
                analyse.categorie(),
                analyse.intitule(),
                analyse.etat(),
                analyse.ageAnnees(),
                analyse.valeurEstimee(),
                cout,
                analyse.ville(),
                analyse.latitude(),
                analyse.longitude(),
                analyse.materiaux(),
                taux);
    }

    private CandidatActeur versCandidat(ActeurRef ref) {
        return new CandidatActeur(
                ref.id(),
                ref.ville(),
                ref.latitude(),
                ref.longitude(),
                ref.noteMoyenne(),
                ref.nbAvis(),
                ref.disponible(),
                ref.competences(),
                ref.categories());
    }

    private ArticleMarche versArticle(ArticleMarcheRef ref) {
        return new ArticleMarche(ref.id(), ref.titre(), ref.categorie(), ref.etat(), ref.prix(),
                ref.description());
    }

    private List<ActeurClasse> filtrerParRayon(List<ActeurClasse> classement, Double rayonKm) {
        if (classement == null) {
            return List.of();
        }
        double rayon = rayonKm != null ? rayonKm : RAYON_DEFAUT_KM;
        List<ActeurClasse> dansLeRayon = classement.stream()
                .filter(c -> c.distanceKm() != null && c.distanceKm() <= rayon)
                .toList();
        return dansLeRayon.isEmpty() ? classement : dansLeRayon;
    }

    private Map<String, Object> resumeValorisation(RecoValorisationResponse reponse) {
        Map<String, Object> resume = new LinkedHashMap<>();
        resume.put("route", reponse.routeRecommandee() == null ? null : reponse.routeRecommandee().name());
        resume.put("probabilite", reponse.probabilite());
        resume.put("confidenceScore", reponse.confidenceScore());
        if (reponse.raisons() != null) {
            resume.put("raisons", reponse.raisons().stream().map(RaisonReco::libelle).toList());
        }
        if (reponse.alternatives() != null) {
            resume.put("alternatives", reponse.alternatives().stream().limit(3)
                    .map(a -> Map.of(
                            "route", a.route() == null ? "" : a.route().name(),
                            "probabilite", a.probabilite() == null ? 0.0 : a.probabilite()))
                    .toList());
        }
        return resume;
    }

    private Map<String, Object> resumeActeurs(RecoActeursResponse reponse) {
        Map<String, Object> resume = new LinkedHashMap<>();
        resume.put("typeActeur", reponse.typeActeur() == null ? "" : reponse.typeActeur().name());
        if (reponse.classement() != null) {
            resume.put("classement", reponse.classement().stream().limit(5)
                    .map(c -> Map.of(
                            "id", c.candidatId() == null ? 0L : c.candidatId(),
                            "score", c.score() == null ? 0.0 : c.score()))
                    .toList());
        }
        return resume;
    }

    private Map<String, Object> resumeMarche(RecoMarcheResponse reponse) {
        Map<String, Object> resume = new LinkedHashMap<>();
        if (reponse.articles() != null) {
            resume.put("articles", reponse.articles().stream().limit(5)
                    .map(a -> Map.of(
                            "id", a.articleId() == null ? 0L : a.articleId(),
                            "titre", a.titre() == null ? "" : a.titre(),
                            "similarite", a.similarite() == null ? 0.0 : a.similarite(),
                            "equitePrix", a.equitePrix() == null ? "" : a.equitePrix()))
                    .toList());
        }
        return resume;
    }

    private HistoriqueReco versHistorique(RecommandationHistorique reco) {
        SourcePrediction source = "ML".equals(reco.source())
                ? SourcePrediction.ML : SourcePrediction.HEURISTIQUE;
        return new HistoriqueReco(
                reco.id(),
                reco.objetId(),
                reco.typeReco(),
                tronquer(reco.chargeUtile()),
                tronquer(reco.resultat()),
                source,
                reco.creeLe());
    }

    private RecoValorisationResponse withId(RecoValorisationResponse reponse, long id) {
        return new RecoValorisationResponse(
                id,
                reponse.routeRecommandee(),
                reponse.probabilite(),
                reponse.alternatives(),
                reponse.raisons(),
                reponse.confidenceScore(),
                reponse.source(),
                reponse.modeleVersion());
    }

    private String tronquer(String texte) {
        if (texte == null) {
            return null;
        }
        return texte.length() <= MAX_TRACTION_CHARGE
                ? texte
                : texte.substring(0, MAX_TRACTION_CHARGE) + "…";
    }

    private String compact(Object valeur) {
        try {
            return mapper.writeValueAsString(valeur);
        } catch (JsonProcessingException e) {
            return String.valueOf(valeur);
        }
    }

    private String valeurOu(String valeur, String parDefaut) {
        return valeur == null || valeur.isBlank() ? parDefaut : valeur;
    }
}
