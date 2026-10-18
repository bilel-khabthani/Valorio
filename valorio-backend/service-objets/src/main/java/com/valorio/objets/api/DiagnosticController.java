package com.valorio.objets.api;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.dto.DiagnosticRequest;
import com.valorio.objets.service.DiagnosticService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/objets/{objetId}/diagnostics")
public class DiagnosticController {

    private final DiagnosticService service;

    public DiagnosticController(DiagnosticService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticObjet diagnostiquer(@PathVariable long objetId,
                                         @Valid @RequestBody DiagnosticRequest request) {
        return service.creer(objetId, request);
    }

    @GetMapping
    public List<DiagnosticObjet> lister(@PathVariable long objetId) {
        return service.lister(objetId);
    }
}
