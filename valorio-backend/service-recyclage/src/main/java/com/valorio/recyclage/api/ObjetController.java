package com.valorio.recyclage.api;

import com.valorio.recyclage.external.ObjetPort;
import com.valorio.recyclage.external.model.ObjetRef;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/objets")
public class ObjetController {

    private final ObjetPort objets;

    public ObjetController(ObjetPort objets) {
        this.objets = objets;
    }

    @GetMapping
    public List<ObjetRef> list(@RequestParam(name = "categorieId", required = false) Long categorieId) {
        return objets.findAll(categorieId);
    }

    @GetMapping("/{id}")
    public ObjetRef get(@PathVariable("id") Long id) {
        return objets.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Objet " + id + " introuvable"));
    }
}
