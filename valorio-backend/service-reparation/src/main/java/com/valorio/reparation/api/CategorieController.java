package com.valorio.reparation.api;

import com.valorio.reparation.external.CategoriePort;
import com.valorio.reparation.external.model.CategorieRef;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategoriePort categories;

    public CategorieController(CategoriePort categories) {
        this.categories = categories;
    }

    @GetMapping("/{id}")
    public CategorieRef get(@PathVariable("id") Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Catégorie " + id + " introuvable"));
    }
}
