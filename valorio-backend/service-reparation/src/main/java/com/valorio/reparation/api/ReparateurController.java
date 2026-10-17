package com.valorio.reparation.api;

import com.valorio.reparation.domain.Reparateur;
import com.valorio.reparation.service.ReparationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reparateurs")
public class ReparateurController {

    private final ReparationService service;

    public ReparateurController(ReparationService service) {
        this.service = service;
    }

    @GetMapping("/recherche")
    public List<Reparateur> rechercher(@RequestParam(required = false) String categorie,
                                       @RequestParam(required = false) String competence,
                                       @RequestParam(required = false) String ville) {
        return service.rechercherReparateurs(categorie, competence, ville);
    }

    @GetMapping("/{reparateurId}")
    public Reparateur consulter(@PathVariable long reparateurId) {
        return service.reparateur(reparateurId);
    }
}
