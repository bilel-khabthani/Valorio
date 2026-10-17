package com.valorio.recyclage.api;

import com.valorio.recyclage.domain.CentreRecyclage;
import com.valorio.recyclage.service.RecyclageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@RequestMapping("/api/centres")
public class CentreController {

    private final RecyclageService service;

    public CentreController(RecyclageService service) {
        this.service = service;
    }

    public record CentreRequest(
            Long organismeId,
            @NotBlank String nom,
            String ville,
            String adresse,
            Double latitude,
            Double longitude,
            @NotEmpty List<String> materiauxAcceptes,
            String horaires
    ) {
    }

    @GetMapping
    public List<CentreRecyclage> rechercher(@RequestParam(required = false) String materiau,
                                            @RequestParam(required = false) Double latitude,
                                            @RequestParam(required = false) Double longitude,
                                            @RequestParam(defaultValue = "50") double rayonKm) {
        return service.rechercherCentres(materiau, latitude, longitude, rayonKm);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentreRecyclage creer(@Valid @RequestBody CentreRequest request) {
        return service.creerCentre(request.organismeId(), request.nom(), request.ville(), request.adresse(),
                request.latitude(), request.longitude(), request.materiauxAcceptes(), request.horaires());
    }

    @GetMapping("/{centreId}")
    public CentreRecyclage consulter(@PathVariable long centreId) {
        return service.detailCentre(centreId);
    }

    @PutMapping("/{centreId}")
    public CentreRecyclage modifier(@PathVariable long centreId, @Valid @RequestBody CentreRequest request) {
        return service.modifierCentre(centreId, request.organismeId(), request.nom(), request.ville(),
                request.adresse(), request.latitude(), request.longitude(), request.materiauxAcceptes(),
                request.horaires());
    }
}
