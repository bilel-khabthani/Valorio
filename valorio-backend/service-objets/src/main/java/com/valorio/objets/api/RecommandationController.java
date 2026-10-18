package com.valorio.objets.api;

import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.Recommandation;
import com.valorio.objets.service.DiagnosticService;
import com.valorio.objets.service.ObjetService;
import com.valorio.objets.service.RecommandationService;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/objets/{objetId}/recommandation")
public class RecommandationController {

    private final ObjetService objets;
    private final DiagnosticService diagnostics;
    private final RecommandationService recommandations;

    public RecommandationController(ObjetService objets,
                                    DiagnosticService diagnostics,
                                    RecommandationService recommandations) {
        this.objets = objets;
        this.diagnostics = diagnostics;
        this.recommandations = recommandations;
    }

    @GetMapping
    public Recommandation recommander(@PathVariable long objetId) {
        Objet objet = objets.detail(objetId);
        DiagnosticObjet diagnostic = diagnostics.dernier(objetId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Aucun diagnostic pour l'objet " + objetId + " — lancez d'abord une analyse"));
        return recommandations.recommander(objet, diagnostic);
    }
}
