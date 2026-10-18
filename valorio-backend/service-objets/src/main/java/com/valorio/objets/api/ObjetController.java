package com.valorio.objets.api;

import com.valorio.objets.domain.EtatObjet;
import com.valorio.objets.domain.EvenementHistorique;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.StatutObjet;
import com.valorio.objets.dto.HistoriqueRequest;
import com.valorio.objets.dto.ObjetRequest;
import com.valorio.objets.dto.StatutRequest;
import com.valorio.objets.service.ObjetService;
import jakarta.validation.Valid;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/objets")
public class ObjetController {

    private final ObjetService service;

    public ObjetController(ObjetService service) {
        this.service = service;
    }

    @GetMapping
    public List<Objet> lister(@RequestParam(required = false) Long categorieId,
                              @RequestParam(required = false) EtatObjet etat,
                              @RequestParam(required = false) String ville,
                              @RequestParam(required = false) StatutObjet statut,
                              @RequestParam(required = false) String q) {
        return service.lister(categorieId, etat, ville, statut, q);
    }

    @GetMapping("/villes")
    public List<String> villes() {
        return service.villes();
    }

    @GetMapping("/{objetId}")
    public Objet consulter(@PathVariable long objetId) {
        return service.detail(objetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Objet declarer(@Valid @RequestBody ObjetRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{objetId}")
    public Objet modifier(@PathVariable long objetId, @Valid @RequestBody ObjetRequest request) {
        return service.modifier(objetId, request);
    }

    @DeleteMapping("/{objetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable long objetId) {
        service.supprimer(objetId);
    }

    @PutMapping("/{objetId}/statut")
    public Objet changerStatut(@PathVariable long objetId,
                               @Valid @RequestBody StatutRequest request,
                               @RequestParam(required = false) Long acteurId) {
        return service.changerStatut(objetId, request.statut(), request.commentaire(), acteurId);
    }

    @PostMapping("/{objetId}/photos")
    @ResponseStatus(HttpStatus.CREATED)
    public Objet ajouterPhotos(@PathVariable long objetId,
                               @RequestParam("photos") List<MultipartFile> photos) {
        return service.ajouterPhotos(objetId, photos);
    }

    @DeleteMapping("/{objetId}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerPhoto(@PathVariable long objetId, @PathVariable long photoId) {
        service.supprimerPhoto(objetId, photoId);
    }

    @GetMapping("/{objetId}/historique")
    public List<EvenementHistorique> historique(@PathVariable long objetId) {
        service.detail(objetId);
        return service.historique(objetId);
    }

    @PostMapping("/{objetId}/historique")
    @ResponseStatus(HttpStatus.CREATED)
    public EvenementHistorique enregistrerEvenement(@PathVariable long objetId,
                                                    @Valid @RequestBody HistoriqueRequest request) {
        return service.enregistrerEvenement(objetId, request.type(), request.libelle(),
                request.acteurId(), request.statutCible());
    }
}
