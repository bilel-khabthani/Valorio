package com.valorio.recommandation.api;

import com.valorio.recommandation.dto.ActeursRequest;
import com.valorio.recommandation.dto.ExplicationResponse;
import com.valorio.recommandation.dto.FeedbackRequest;
import com.valorio.recommandation.dto.FeedbackResponse;
import com.valorio.recommandation.dto.HistoriqueReco;
import com.valorio.recommandation.dto.MarcheRequest;
import com.valorio.recommandation.dto.ModeleMetadata;
import com.valorio.recommandation.dto.RecoActeursResponse;
import com.valorio.recommandation.dto.RecoMarcheResponse;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import com.valorio.recommandation.dto.ValorisationRequest;
import com.valorio.recommandation.service.RecommandationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommandations")
@Tag(name = "Recommandation", description = "Recommandations intelligentes Valorio : valorisation, acteurs, marché")
public class RecommandationController {

    private final RecommandationService service;

    public RecommandationController(RecommandationService service) {
        this.service = service;
    }

    @PostMapping("/valorisation")
    @Operation(summary = "Recommander la meilleure route de valorisation d'un objet")
    public RecoValorisationResponse recommanderValorisation(@Valid @RequestBody ValorisationRequest request) {
        return service.recommanderValorisation(request);
    }

    @PostMapping("/acteurs")
    @Operation(summary = "Classer les acteurs pertinents pour une demande")
    public RecoActeursResponse recommanderActeurs(@Valid @RequestBody ActeursRequest request) {
        return service.recommanderActeurs(request);
    }

    @PostMapping("/marche/similaires")
    @Operation(summary = "Trouver les articles de marché similaires")
    public RecoMarcheResponse articlesSimilaires(@Valid @RequestBody MarcheRequest request) {
        return service.articlesSimilaires(request);
    }

    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer le retour utilisateur sur une recommandation")
    public FeedbackResponse enregistrerFeedback(@Valid @RequestBody FeedbackRequest request) {
        return service.enregistrerFeedback(request);
    }

    @GetMapping("/modele/metadata")
    @Operation(summary = "Métadonnées du modèle actif (version, métriques, top features)")
    public ModeleMetadata metadataModele() {
        return service.metadata();
    }

    @GetMapping("/historique")
    @Operation(summary = "Historique des recommandations émises (filtre par objet)")
    public List<HistoriqueReco> historique(@RequestParam(name = "objetId", required = false) Long objetId) {
        return service.historique(objetId);
    }

    @PostMapping("/{recommandationId}/explication")
    @Operation(summary = "Générer une narration en langage naturel d'une recommandation")
    public ExplicationResponse expliquer(@PathVariable long recommandationId) {
        return service.expliquer(recommandationId);
    }
}
