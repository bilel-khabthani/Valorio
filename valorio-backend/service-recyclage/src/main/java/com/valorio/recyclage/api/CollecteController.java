package com.valorio.recyclage.api;

import com.valorio.recyclage.domain.DemandeCollecte;
import com.valorio.recyclage.domain.StatutCollecte;
import com.valorio.recyclage.service.RecyclageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/collectes")
public class CollecteController {

    private final RecyclageService service;

    public CollecteController(RecyclageService service) {
        this.service = service;
    }

    public record DemandeCollecteRequest(
            @NotNull Long objetId,
            @NotNull Long utilisateurId,
            Long centreId,
            OffsetDateTime datePrevue
    ) {
    }

    public record TransitionCollecteRequest(
            @NotNull StatutCollecte statut,
            OffsetDateTime datePrevue,
            String commentaire
    ) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeCollecte demander(@Valid @RequestBody DemandeCollecteRequest request) {
        return service.demanderCollecte(request.objetId(), request.utilisateurId(), request.centreId(),
                request.datePrevue());
    }

    @GetMapping
    public List<DemandeCollecte> lister(@RequestParam(required = false) StatutCollecte statut,
                                        @RequestParam(required = false) Long utilisateurId,
                                        @RequestParam(required = false) Long centreId) {
        return service.listerCollectes(statut, utilisateurId, centreId);
    }

    @GetMapping("/{collecteId}")
    public DemandeCollecte consulter(@PathVariable long collecteId) {
        return service.chargerCollecte(collecteId);
    }

    @PutMapping("/{collecteId}/statut")
    public DemandeCollecte changerStatut(@PathVariable long collecteId,
                                         @Valid @RequestBody TransitionCollecteRequest request) {
        return service.changerStatutCollecte(collecteId, request.statut(), request.datePrevue(),
                request.commentaire());
    }
}
