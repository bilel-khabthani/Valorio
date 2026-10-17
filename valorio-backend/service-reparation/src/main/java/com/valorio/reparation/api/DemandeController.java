package com.valorio.reparation.api;

import com.valorio.reparation.domain.Devis;
import com.valorio.reparation.domain.DemandeReparation;
import com.valorio.reparation.domain.StatutDemande;
import com.valorio.reparation.service.ReparationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final ReparationService service;

    public DemandeController(ReparationService service) {
        this.service = service;
    }

    public record CreerDemandeRequest(
            @NotNull Long objetId,
            @NotNull Long utilisateurId,
            @NotBlank String descriptionPanne
    ) {
    }

    public record TransitionStatutRequest(
            @NotNull StatutDemande statut,
            String commentaire
    ) {
    }

    public record PlanifierInterventionRequest(
            @NotNull OffsetDateTime dateIntervention
    ) {
    }

    public record CreerDevisRequest(
            @NotNull @Positive BigDecimal montant,
            Integer delaiJours,
            String commentaire,
            Long reparateurId
    ) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeReparation creer(@Valid @RequestBody CreerDemandeRequest request) {
        return service.creerDemande(request.objetId(), request.utilisateurId(), request.descriptionPanne());
    }

    @GetMapping
    public List<DemandeReparation> lister(@RequestParam(required = false) StatutDemande statut,
                                          @RequestParam(required = false) Long utilisateurId,
                                          @RequestParam(required = false) Long reparateurId) {
        return service.lister(statut, utilisateurId, reparateurId);
    }

    @GetMapping("/{demandeId}")
    public DemandeReparation consulter(@PathVariable long demandeId) {
        return service.charger(demandeId);
    }

    @PutMapping("/{demandeId}/statut")
    public DemandeReparation changerStatut(@PathVariable long demandeId,
                                           @Valid @RequestBody TransitionStatutRequest request) {
        return service.changerStatut(demandeId, request.statut(), request.commentaire());
    }

    @PutMapping("/{demandeId}/intervention")
    public DemandeReparation planifierIntervention(@PathVariable long demandeId,
                                                   @Valid @RequestBody PlanifierInterventionRequest request) {
        return service.planifierIntervention(demandeId, request.dateIntervention());
    }

    @PostMapping("/{demandeId}/devis")
    @ResponseStatus(HttpStatus.CREATED)
    public Devis creerDevis(@PathVariable long demandeId, @Valid @RequestBody CreerDevisRequest request) {
        return service.creerDevis(demandeId, request.montant(), request.delaiJours(),
                request.commentaire(), request.reparateurId());
    }

    @GetMapping("/{demandeId}/devis")
    public Devis devis(@PathVariable long demandeId) {
        return service.devisDeDemande(demandeId);
    }
}
