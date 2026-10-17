package com.valorio.don.api;

import com.valorio.don.domain.DemandeDon;
import com.valorio.don.domain.StatutDemandeDon;
import com.valorio.don.service.DonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes-don")
public class DemandeDonController {

    private final DonService service;

    public DemandeDonController(DonService service) {
        this.service = service;
    }

    public record TransitionDonRequest(
            @NotNull StatutDemandeDon statut,
            String commentaire
    ) {
    }

    @GetMapping("/{demandeId}")
    public DemandeDon consulter(@PathVariable long demandeId) {
        return service.chargerDemande(demandeId);
    }

    @PutMapping("/{demandeId}/statut")
    public DemandeDon changerStatut(@PathVariable long demandeId,
                                    @Valid @RequestBody TransitionDonRequest request) {
        return service.changerStatutDemande(demandeId, request.statut(), request.commentaire());
    }
}
