package com.valorio.recyclage.api;

import com.valorio.recyclage.external.UtilisateurPort;
import com.valorio.recyclage.external.model.UtilisateurRef;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurPort utilisateurs;

    public UtilisateurController(UtilisateurPort utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<UtilisateurRef> list() {
        return utilisateurs.findAll();
    }

    @GetMapping("/{id}")
    public UtilisateurRef get(@PathVariable("id") Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + id + " introuvable"));
    }
}
