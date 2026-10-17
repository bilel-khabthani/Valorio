package com.valorio.reparation.api;

import com.valorio.reparation.domain.Devis;
import com.valorio.reparation.service.ReparationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devis")
public class DevisController {

    private final ReparationService service;

    public DevisController(ReparationService service) {
        this.service = service;
    }

    public record ReponseDevisRequest(
            @NotNull Boolean accepte
    ) {
    }

    @PutMapping("/{devisId}/reponse")
    public Devis repondre(@PathVariable long devisId, @Valid @RequestBody ReponseDevisRequest request) {
        return service.repondreDevis(devisId, request.accepte());
    }
}
