package com.valorio.reparation.ai.api;

import com.valorio.reparation.ai.model.DiagnosticRequest;
import com.valorio.reparation.ai.model.DiagnosticResponse;
import com.valorio.reparation.ai.security.JwtGuard;
import com.valorio.reparation.ai.service.DiagnosticService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose l'assistant IA de diagnostic de panne (module Réparation).
 * L'accès est réservé aux réparateurs authentifiés : vérification JWT via
 * {@link JwtGuard} (acteur REPARATEUR ou ADMIN).
 */
@RestController
@RequestMapping("/api/diagnostic-ia")
public class DiagnosticController {

    private final DiagnosticService service;
    private final JwtGuard guard;

    public DiagnosticController(DiagnosticService service, JwtGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @PostMapping
    public DiagnosticResponse diagnostiquer(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody DiagnosticRequest request) {
        guard.exigerReparateur(authorization);
        return service.diagnostiquer(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("statut", "UP", "fournisseur", service.fournisseurActif());
    }
}
