package com.valorio.objets.api;

import com.valorio.objets.external.UtilisateurPort;
import com.valorio.objets.external.model.UtilisateurRef;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UtilisateurController {

    private final UtilisateurPort utilisateurs;

    public UtilisateurController(UtilisateurPort utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<UtilisateurRef> lister() {
        return utilisateurs.findAll();
    }

    @GetMapping("/{userId}")
    public UtilisateurRef consulter(@PathVariable long userId) {
        return utilisateurs.findById(userId).orElse(null);
    }
}
