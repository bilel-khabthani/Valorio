package com.valorio.auth.api;

import com.valorio.auth.domain.CibleSignalement;
import com.valorio.auth.domain.Signalement;
import com.valorio.auth.domain.StatutSignalement;
import com.valorio.auth.dto.SignalementRequest;
import com.valorio.auth.dto.SignalementStats;
import com.valorio.auth.dto.TraitementSignalementRequest;
import com.valorio.auth.security.UserPrincipal;
import com.valorio.auth.service.SignalementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Signalements (module 1, sous-module 1.3) : création par tout utilisateur, traitement par les administrateurs. */
@RestController
@RequestMapping("/api/signalements")
@Tag(name = "Signalements", description = "Signalements de comportements et de contenus")
public class SignalementController {

    private final SignalementService service;

    public SignalementController(SignalementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un signalement")
    public Signalement creer(@Valid @RequestBody SignalementRequest request, Authentication authentication) {
        return service.creer(request, identifiant(authentication));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les signalements (administration)")
    public List<Signalement> lister(@RequestParam(required = false) StatutSignalement statut,
                                    @RequestParam(required = false) CibleSignalement cibleType) {
        return service.lister(statut, cibleType);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques des signalements (administration)")
    public SignalementStats statistiques() {
        return service.statistiques();
    }

    @GetMapping("/{signalementId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consulter un signalement (administration)")
    public Signalement consulter(@PathVariable long signalementId) {
        return service.detail(signalementId);
    }

    @PutMapping("/{signalementId}/traiter")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Traiter un signalement (administration)")
    public Signalement traiter(@PathVariable long signalementId,
                               @Valid @RequestBody TraitementSignalementRequest request,
                               Authentication authentication) {
        return service.traiter(signalementId, request.statut(), identifiant(authentication), request.note());
    }

    private Long identifiant(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }
}
