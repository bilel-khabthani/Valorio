package com.valorio.don.api;

import com.valorio.don.domain.AnnonceDon;
import com.valorio.don.domain.DemandeDon;
import com.valorio.don.domain.StatutAnnonceDon;
import com.valorio.don.service.DonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/dons")
public class DonController {

    private final DonService service;

    public DonController(DonService service) {
        this.service = service;
    }

    public record AnnonceDonRequest(
            @NotNull Long objetId,
            @NotNull Long donneurId,
            @NotBlank String titre,
            String description,
            String conditions,
            String ville,
            Double latitude,
            Double longitude
    ) {
    }

    public record DemandeDonRequest(
            @NotNull Long beneficiaireId,
            String message
    ) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnonceDon publier(@Valid @RequestBody AnnonceDonRequest request) {
        return service.publier(request.objetId(), request.donneurId(), request.titre(), request.description(),
                request.conditions(), request.ville(), request.latitude(), request.longitude());
    }

    @GetMapping
    public List<AnnonceDon> lister(@RequestParam(required = false) Long categorieId,
                                   @RequestParam(required = false) String ville,
                                   @RequestParam(required = false) StatutAnnonceDon statut) {
        return service.lister(categorieId, ville, statut);
    }

    @GetMapping("/matching")
    public List<DonService.Correspondance> matching(@RequestParam(required = false) Long categorieId,
                                                    @RequestParam(required = false) Double latitude,
                                                    @RequestParam(required = false) Double longitude,
                                                    @RequestParam(defaultValue = "50") double rayonKm) {
        return service.matching(categorieId, latitude, longitude, rayonKm);
    }

    @GetMapping("/{annonceId}")
    public AnnonceDon consulter(@PathVariable long annonceId) {
        return service.detail(annonceId);
    }

    @PutMapping("/{annonceId}")
    public AnnonceDon modifier(@PathVariable long annonceId, @Valid @RequestBody AnnonceDonRequest request) {
        return service.modifier(annonceId, request.titre(), request.description(), request.conditions(),
                request.ville(), request.latitude(), request.longitude());
    }

    @PutMapping("/{annonceId}/annuler")
    public AnnonceDon annuler(@PathVariable long annonceId,
                              @RequestParam(required = false) Long donneurId) {
        return service.annuler(annonceId, donneurId);
    }

    @DeleteMapping("/{annonceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable long annonceId,
                          @RequestParam(required = false) Long donneurId) {
        service.supprimer(annonceId, donneurId);
    }

    @PostMapping("/{annonceId}/demandes")
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeDon demander(@PathVariable long annonceId, @Valid @RequestBody DemandeDonRequest request) {
        return service.demander(annonceId, request.beneficiaireId(), request.message());
    }

    @GetMapping("/{annonceId}/demandes")
    public List<DemandeDon> demandes(@PathVariable long annonceId) {
        return service.demandesDeAnnonce(annonceId);
    }
}
